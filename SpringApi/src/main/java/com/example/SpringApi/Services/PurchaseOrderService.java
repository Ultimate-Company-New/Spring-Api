package com.example.SpringApi.Services;

import com.example.SpringApi.Constants.EntityType;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.PurchaseOrderFilterQueryBuilder;
import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Helpers.HTMLHelper;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Helpers.PDFHelper;
import com.example.SpringApi.Models.ApiRoutes;
import com.itextpdf.text.DocumentException;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.PaymentInfo;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderQuantityPriceMap;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Repositories.PaymentInfoRepository;
import com.example.SpringApi.Repositories.PurchaseOrderRepository;
import com.example.SpringApi.Repositories.PurchaseOrderQuantityPriceMapRepository;
import com.example.SpringApi.Repositories.ResourcesRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.IPurchaseOrderSubTranslator;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for PurchaseOrder operations.
 * 
 * This service handles all business logic related to purchase order management
 * including CRUD operations, approval workflow, and PDF generation.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PurchaseOrderService extends BaseService implements IPurchaseOrderSubTranslator {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final ClientRepository clientRepository;
    private final ResourcesRepository resourcesRepository;
    private final PurchaseOrderQuantityPriceMapRepository purchaseOrderQuantityPriceMapRepository;
    private final UserLogService userLogService;
    private final Environment environment;
    private final PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder;
    private final MessageService messageService;
    
    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                               PaymentInfoRepository paymentInfoRepository,
                               AddressRepository addressRepository,
                               UserRepository userRepository,
                               LeadRepository leadRepository,
                               ClientRepository clientRepository,
                               ResourcesRepository resourcesRepository,
                               PurchaseOrderQuantityPriceMapRepository purchaseOrderQuantityPriceMapRepository,
                               UserLogService userLogService,
                               PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder,
                               MessageService messageService,
                               Environment environment) {
        super();
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.paymentInfoRepository = paymentInfoRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.clientRepository = clientRepository;
        this.resourcesRepository = resourcesRepository;
        this.purchaseOrderQuantityPriceMapRepository = purchaseOrderQuantityPriceMapRepository;
        this.userLogService = userLogService;
        this.purchaseOrderFilterQueryBuilder = purchaseOrderFilterQueryBuilder;
        this.messageService = messageService;
        this.environment = environment;
    }
    
    /**
     * Retrieves purchase orders in batches with pagination support.
     * 
     * This method returns a paginated list of purchase orders based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * 
     * Eagerly loads all related entities including:
     * - Address (delivery/billing address)
     * - PaymentInfo (payment details and quotation)
     * - Created By User
     * - Modified By User
     * - Assigned Lead
     * - Approved By User
     * - Purchase Order Quantity Maps with Product Pickup Location Mappings, Products, and Pickup Locations
     * 
     * Advanced Filtering Capabilities:
     * - selectedProductIds: Filter POs containing specific products
     * - address: Filter by shipping address (combined street, city, state, postalCode, country)
     * - All standard PO fields (status, amounts, dates, vendor info, etc.)
     * 
     * @param paginationBaseRequestModel The pagination parameters including page size, number, filters, and sorting
     * @return Paginated response containing purchase order data with all related entities
     * @throws BadRequestException if validation fails
     */
    @Override
    public PaginationBaseResponseModel<PurchaseOrderResponseModel> getPurchaseOrdersInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Valid columns for filtering - includes PO fields and address field
        Set<String> validColumns = Set.of(
            // Purchase Order fields
            "purchaseOrderId", "vendorNumber", "purchaseOrderStatus", "priority", "paymentId",
            "expectedDeliveryDate", "createdAt", "updatedAt", "purchaseOrderReceipt", 
            "termsConditionsHtml", "notes", "createdUser", "modifiedUser",
            "approvedByUserId", "approvedDate", "rejectedByUserId", "rejectedDate",
            "isDeleted", "address"
        );

        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException("Invalid column name: " + filter.getColumn());
                }

                // Validate operator
                Set<String> validOperators = new HashSet<>(Arrays.asList(
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
                ));
                if (filter.getOperator() != null && !validOperators.contains(filter.getOperator())) {
                    throw new BadRequestException("Invalid operator: " + filter.getOperator());
                }

                // Validate column type matches operator
                String columnType = purchaseOrderFilterQueryBuilder.getColumnType(filter.getColumn());
                if ("boolean".equals(columnType) && !filter.getOperator().equals("equals") && !filter.getOperator().equals("notEquals")) {
                    throw new BadRequestException("Boolean columns only support 'equals' and 'notEquals' operators");
                }
                if ("date".equals(columnType) || "number".equals(columnType)) {
                    Set<String> numericDateOperators = new HashSet<>(Arrays.asList(
                        "equals", "notEquals", "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
                    ));
                    if (!numericDateOperators.contains(filter.getOperator())) {
                        throw new BadRequestException(columnType + " columns only support numeric comparison operators");
                    }
                }
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException("Invalid pagination: end must be greater than start");
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("purchaseOrderId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // selectedProductIds can be passed as a separate parameter if needed in the future
        // For now, we'll use null to indicate no product filtering
        List<Long> selectedProductIds = null;

        // Use filter query builder for dynamic filtering
        Page<PurchaseOrder> page = purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            selectedProductIds,
            paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Load resources (attachments) for all purchase orders in this page
        if (!page.getContent().isEmpty()) {
            // Load resources individually for each PO filtered by entityType
            for (PurchaseOrder po : page.getContent()) {
                List<Resources> poResources = resourcesRepository.findByEntityIdAndEntityType(
                    po.getPurchaseOrderId(), EntityType.PURCHASE_ORDER);
                po.setAttachments(poResources);
            }
        }

        // Convert PurchaseOrder entities to PurchaseOrderResponseModel
        List<PurchaseOrderResponseModel> purchaseOrderResponseModels = page.getContent().stream()
            .map(PurchaseOrderResponseModel::new)
            .toList();

        PaginationBaseResponseModel<PurchaseOrderResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(purchaseOrderResponseModels);
        response.setTotalDataCount(page.getTotalElements());

        return response;
    }
    
    /**
     * Creates a new purchase order.
     * 
     * This method creates a new purchase order with the provided details including
     * supplier information, line items, and delivery details.
     * All validations are performed in the PurchaseOrder entity constructor.
     * 
     * Flow:
     * 1. Create Address (if address data provided) or use existing purchaseOrderAddressId
     * 2. Create PurchaseOrder entity (without paymentId)
     * 3. Save PurchaseOrder to get purchaseOrderId
     * 4. Create PaymentInfo quotation from PurchaseOrder
     * 5. Save PaymentInfo to get paymentId
     * 6. Link paymentId to PurchaseOrder and update
     * 
     * @param purchaseOrderRequestModel The purchase order to create
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user is not authorized
     */
    @Override
    public void createPurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
        // Step 1: Create the purchase order entity (validations are done in constructor)
        PurchaseOrder purchaseOrder = new PurchaseOrder(purchaseOrderRequestModel, getUser(), getClientId());
        
        // Step 2: Create the address entity (validations are done in constructor)
        Address address = new Address(purchaseOrderRequestModel.getAddress(), getUser());
        addressRepository.save(address);
        purchaseOrder.setPurchaseOrderAddressId(address.getAddressId());

        // Step 3: Create the payment info entity BEFORE saving purchase order (paymentId is non-nullable)
        PaymentInfo paymentInfo = new PaymentInfo(purchaseOrderRequestModel, getUser());
        paymentInfo = paymentInfoRepository.save(paymentInfo);
        purchaseOrder.setPaymentId(paymentInfo.getPaymentId());

        // Step 4: Save the purchase order to database (to get purchaseOrderId)
        purchaseOrderRepository.save(purchaseOrder);

        // Step 5: Create the purchase order quantity price map entities
        List<PurchaseOrderQuantityPriceMap> purchaseOrderQuantityPriceMaps = PurchaseOrderQuantityPriceMap.createFromRequest(
            purchaseOrderRequestModel,
            purchaseOrder.getPurchaseOrderId()
        );
        purchaseOrderQuantityPriceMapRepository.saveAll(purchaseOrderQuantityPriceMaps);
        
        // Step 6: Handle attachments if provided
        if (purchaseOrderRequestModel.getAttachments() != null && 
            !purchaseOrderRequestModel.getAttachments().isEmpty()) {
            
            uploadPurchaseOrderAttachments(
                purchaseOrderRequestModel.getAttachments(),
                purchaseOrder.getPurchaseOrderId()
            );
        }
        
        // Log the creation
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.InsertPurchaseOrder + " " + purchaseOrder.getPurchaseOrderId() + 
            " with Address " + purchaseOrder.getPurchaseOrderAddressId() +
            " and Payment Quotation " + paymentInfo.getPaymentId(),
            ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER);
    }
    
    /**
     * Updates an existing purchase order.
     * 
     * This method updates an existing purchase order's details including
     * supplier information, line items, and delivery details.
     * 
     * Flow:
     * 1. Fetch existing PurchaseOrder
     * 2. Handle Address update if new address data provided
     * 3. Update PurchaseOrder with new data
     * 4. Save PurchaseOrder
     * 5. If payment quotation exists, update it with recalculated amounts
     * 6. If no payment quotation exists, create one
     * 
     * @param purchaseOrderRequestModel The purchase order to update
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user is not authorized
     */
    @Override
    public void updatePurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
        // Step 1: Update the purchase order entity
        PurchaseOrder existingPurchaseOrder = purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                purchaseOrderRequestModel.getPurchaseOrderId(), 
                getClientId()
            )
            .orElseThrow(() -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId));
        PurchaseOrder updatedPurchaseOrder = new PurchaseOrder(
            purchaseOrderRequestModel, 
            getUser(), 
            existingPurchaseOrder
        ); 
        purchaseOrderRepository.save(updatedPurchaseOrder);
        
        // Step 2: Handle Address update if new address data provided
        Address existingAddress = addressRepository.findById(updatedPurchaseOrder.getPurchaseOrderAddressId())
        .orElseThrow(() -> new NotFoundException(ErrorMessages.AddressErrorMessages.InvalidId));    
        Address updatedAddress = new Address(purchaseOrderRequestModel.getAddress(), getUser(), existingAddress);
        addressRepository.save(updatedAddress);

        // Step 3: Update the purchase order quantity price map entities
        updatePurchaseOrderQuantityPriceMaps(
            updatedPurchaseOrder.getPurchaseOrderId(),
            purchaseOrderRequestModel
        );
        
        // Step 4: Update or create payment quotation
        PaymentInfo existingPaymentInfo = paymentInfoRepository.findById(updatedPurchaseOrder.getPaymentId())
        .orElseThrow(() -> new NotFoundException(ErrorMessages.PaymentInfoErrorMessages.InvalidId));
        PaymentInfo updatedPaymentInfo = new PaymentInfo(
                purchaseOrderRequestModel, 
                getUser(), 
                existingPaymentInfo
            );
        paymentInfoRepository.save(updatedPaymentInfo);
        
        // Step 5: Update the Resources (attachments)
        // Delete all existing resources from ImgBB and database, then create new ones
        deleteExistingPurchaseOrderAttachments(updatedPurchaseOrder.getPurchaseOrderId());
        
        // Create new resources if attachments are provided
        if (purchaseOrderRequestModel.getAttachments() != null && 
            !purchaseOrderRequestModel.getAttachments().isEmpty()) {
            
            uploadPurchaseOrderAttachments(
                purchaseOrderRequestModel.getAttachments(),
                updatedPurchaseOrder.getPurchaseOrderId()
            );
        }
        
        // Log the update
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.UpdatePurchaseOrder + " " + updatedPurchaseOrder.getPurchaseOrderId(),
            ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER);
    }
    
    /**
     * Retrieves detailed information about a specific purchase order by ID.
     * 
     * This method returns comprehensive purchase order details including all related entities:
     * - Address (delivery/billing address)
     * - PaymentInfo (payment details and quotation)
     * - Created By User
     * - Modified By User
     * - Assigned Lead
     * - Approved By User
     * - Products with quantities and pickup locations
     * 
     * @param id The ID of the purchase order to retrieve
     * @return The purchase order details with all relationships
     * @throws NotFoundException if the purchase order is not found or doesn't belong to the current client
     */
    @Override
    public PurchaseOrderResponseModel getPurchaseOrderDetailsById(long id) {
        // Fetch purchase order with all relationships and validate it belongs to current client
        Optional<PurchaseOrder> purchaseOrderOptional = 
            purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(id, getClientId());
        
        if (purchaseOrderOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId);
        }
        
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        
        // Load resources (attachments) for this purchase order filtered by entityType
        List<Resources> resources = resourcesRepository.findByEntityIdAndEntityType(id, EntityType.PURCHASE_ORDER);
        purchaseOrder.setAttachments(resources);
        
        // Convert to response model (constructor handles all mapping)
        return new PurchaseOrderResponseModel(purchaseOrder);
    }
    
    /**
     * Toggles the deleted status of a purchase order (soft delete/restore).
     * 
     * This method toggles the deleted flag of a purchase order without permanently
     * removing it from the database. Deleted purchase orders are hidden from standard queries.
     * 
     * @param id The ID of the purchase order to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found or doesn't belong to the current client
     */
    @Override
    public void togglePurchaseOrder(long id) {
        // Validate purchase order exists and belongs to current client
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
        if (purchaseOrderOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId);
        }
        
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        
        // Toggle the isDeleted flag
        purchaseOrder.setIsDeleted(!purchaseOrder.getIsDeleted());
        
        // Update modified user
        purchaseOrder.setModifiedUser(getUser());
        
        // Save the updated purchase order
        purchaseOrderRepository.save(purchaseOrder);
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.TogglePurchaseOrder + " " + purchaseOrder.getPurchaseOrderId(),
            ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER);
    }
    
    /**
     * Approves a purchase order.
     * 
     * This method marks a purchase order as approved by setting the approvedByUserId
     * to the current user's ID, allowing it to proceed to the next stage in the procurement workflow.
     * 
     * @param id The ID of the purchase order to approve
     * @throws NotFoundException if the purchase order is not found or doesn't belong to the current client
     * @throws BadRequestException if the purchase order is already approved
     */
    @Override
    public void approvedByPurchaseOrder(long id) {
        // Validate purchase order exists and belongs to current client
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
        if (purchaseOrderOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId);
        }
        
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        
        // Check if purchase order is already approved
        if (purchaseOrder.getApprovedByUserId() != null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.AlreadyApproved);
        }
        
        // Set approval fields (also clears rejection fields and updates modified user)
        purchaseOrder.setApprovalFields(getUser(), getUserId());
        
        // Save the updated purchase order
        purchaseOrderRepository.save(purchaseOrder);
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.SetApprovedByPurchaseOrder + " PO: " + purchaseOrder.getPurchaseOrderId(),
            ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER);
    }
    
    /**
     * Rejects a purchase order.
     * 
     * This method marks a purchase order as rejected by setting the rejectedByUserId
     * to the current user's ID, preventing it from proceeding in the procurement workflow.
     * 
     * @param id The ID of the purchase order to reject
     * @throws NotFoundException if the purchase order is not found or doesn't belong to the current client
     * @throws BadRequestException if the purchase order is already rejected
     */
    @Override
    public void rejectedByPurchaseOrder(long id) {
        // Validate purchase order exists and belongs to current client
        Optional<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
        if (purchaseOrderOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId);
        }
        
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        
        // Check if purchase order is already rejected
        if (purchaseOrder.getRejectedByUserId() != null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.AlreadyRejected);
        }
        
        // Set rejection fields (also clears approval fields and updates modified user)
        purchaseOrder.setRejectionFields(getUser(), getUserId());
        
        // Save the updated purchase order
        purchaseOrderRepository.save(purchaseOrder);
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.SetRejectedByPurchaseOrder + " PO: " + purchaseOrder.getPurchaseOrderId(),
            ApiRoutes.PurchaseOrderSubRoute.REJECTED_BY_PURCHASE_ORDER);
    }
    
    /**
     * Generates a PDF document for a purchase order.
     * 
     * This method generates a formatted PDF document containing all purchase order
     * details including supplier information, line items, totals, and terms.
     * 
     * @param id The ID of the purchase order to generate PDF for
     * @return The PDF as a byte array
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws TemplateException if PDF template processing fails
     * @throws IOException if PDF generation fails
     * @throws DocumentException if PDF document creation fails
     */
    @Override
    public byte[] getPurchaseOrderPDF(long id) throws TemplateException, IOException, DocumentException {
        // Fetch purchase order with all relationships
        Optional<PurchaseOrder> purchaseOrderOptional = 
            purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(id, getClientId());
        
        if (purchaseOrderOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId);
        }
        
        PurchaseOrder purchaseOrder = purchaseOrderOptional.get();
        
        // Fetch shipping address
        Optional<Address> shippingAddressOptional = 
            addressRepository.findById(purchaseOrder.getPurchaseOrderAddressId());
        
        if (shippingAddressOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.InvalidId);
        }
        
        Address shippingAddress = shippingAddressOptional.get();
        
        // Fetch created by user
        Optional<User> purchaseOrderCreatedByOptional = 
            userRepository.findByUserIdAndClientId(getUserId(), getClientId());
        
        if (purchaseOrderCreatedByOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
        }
        
        User purchaseOrderCreatedBy = purchaseOrderCreatedByOptional.get();
        
        // Fetch approved by user (if approved)
        User purchaseOrderApprovedBy = null;
        if (purchaseOrder.getApprovedByUserId() != null) {
            Optional<User> purchaseOrderApprovedByOptional = 
                userRepository.findByUserIdAndClientId(purchaseOrder.getApprovedByUserId(), getClientId());
            
            if (purchaseOrderApprovedByOptional.isEmpty()) {
                throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
            }
            
            purchaseOrderApprovedBy = purchaseOrderApprovedByOptional.get();
        }
        
        // Fetch lead
        Lead lead = leadRepository.findLeadWithDetailsByIdIncludingDeleted(
            purchaseOrder.getAssignedLeadId(), 
            getClientId()
        );
        
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LeadsErrorMessages.InvalidId);
        }
        
        // Fetch client details
        Optional<Client> clientOptional = clientRepository.findById(getClientId());
        
        if (clientOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
        
        Client client = clientOptional.get();
        
        // Get product quantity map
        Map<Product, Integer> productQuantityMap = getProductQuantityMap(purchaseOrder);
        
        // Generate HTML from template
        String htmlContent = formPurchaseOrderPdf(
            client,
            purchaseOrder,
            shippingAddress,
            purchaseOrderCreatedBy,
            purchaseOrderApprovedBy,
            lead,
            productQuantityMap
        );
        
        // Replace br tags for PDF compatibility
        htmlContent = HTMLHelper.replaceBrTags(htmlContent);
        
        // Convert HTML to PDF
        String logoFilePath = (environment.getActiveProfiles().length > 0 
            ? environment.getActiveProfiles()[0] 
            : "default") + "/" + client.getName() + "/Logo.png";
        
        byte[] pdfBytes = PDFHelper.convertPurchaseOrderHtmlToPdf(
            client.getGoogleCred(),
            logoFilePath,
            htmlContent
        );
        
        // Log the PDF generation
        userLogService.logData(
            getUserId(),
            SuccessMessages.PurchaseOrderSuccessMessages.GetPurchaseOrderPdf + " " + id,
            ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_PDF
        );
        
        // Return PDF as byte array
        return pdfBytes;
    }
    
    /**
     * Forms the HTML content for the purchase order PDF using FreeMarker template.
     * 
     * @param client The client (company) information
     * @param purchaseOrder The purchase order entity
     * @param shippingAddress The shipping address
     * @param purchaseOrderCreatedBy The user who created the purchase order
     * @param purchaseOrderApprovedBy The user who approved the purchase order (can be null)
     * @param lead The lead associated with the purchase order
     * @param productQuantityMap Map of products to quantities
     * @return The HTML content as a string
     * @throws IOException if template loading fails
     * @throws TemplateException if template processing fails
     */
    private String formPurchaseOrderPdf(
            Client client,
            PurchaseOrder purchaseOrder,
            Address shippingAddress,
            User purchaseOrderCreatedBy,
            User purchaseOrderApprovedBy,
            Lead lead,
            Map<Product, Integer> productQuantityMap) throws IOException, TemplateException {
        
        // Configure FreeMarker
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassLoaderForTemplateLoading(
            Thread.currentThread().getContextClassLoader(),
            "InvoiceTemplates"
        );
        
        // Load template
        Template template = cfg.getTemplate("PurchaseOrder.ftl");
        
        // Prepare template data
        Map<String, Object> templateData = new HashMap<>();
        
        // Company information
        templateData.put("companyName", client.getName());
        templateData.put("website", client.getWebsite() != null ? client.getWebsite() : "");
        templateData.put("fullAddress", client.getSendGridEmailAddress() != null 
            ? client.getSendGridEmailAddress() 
            : client.getSupportEmail());
        templateData.put("supportEmail", client.getSupportEmail() != null ? client.getSupportEmail() : "");
        
        // Company logo URL (from ImgBB or other source)
        templateData.put("companyLogo", client.getLogoUrl() != null ? client.getLogoUrl() : "");
        
        // Current year for footer
        templateData.put("currentYear", java.time.Year.now().getValue());
        
        // Purchase order details
        templateData.put("purchaseOrder", purchaseOrder);
        templateData.put("shippingAddress", shippingAddress);
        templateData.put("lead", lead);
        templateData.put("purchaseOrderCreatedBy", purchaseOrderCreatedBy);
        
        // Approved by user (handle null case)
        if (purchaseOrderApprovedBy != null) {
            templateData.put("purchaseOrderApprovedBy", purchaseOrderApprovedBy);
        } else {
            // Create a placeholder user for template
            User placeholderUser = new User();
            placeholderUser.setFirstName("Not");
            placeholderUser.setLastName("Approved");
            placeholderUser.setLoginName("N/A");
            placeholderUser.setRole("N/A");
            placeholderUser.setPhone("N/A");
            templateData.put("purchaseOrderApprovedBy", placeholderUser);
        }
        
        // Product quantity map
        templateData.put("purchaseOrdersProductQuantityMaps", productQuantityMap);
        
        // Process template
        StringWriter out = new StringWriter();
        template.process(templateData, out);
        
        return out.toString();
    }
    
    /**
     * Retrieves the product to quantity mapping for a purchase order.
     * 
     * @param purchaseOrder The purchase order entity
     * @return Map of Product to quantity
     */
    private Map<Product, Integer> getProductQuantityMap(PurchaseOrder purchaseOrder) {
        Map<Product, Integer> productQuantityMap = new LinkedHashMap<>();
        
        // Get all quantity price maps for this purchase order
        Set<PurchaseOrderQuantityPriceMap> quantityMaps = purchaseOrder.getPurchaseOrderQuantityPriceMaps();
        
        if (quantityMaps != null) {
            for (PurchaseOrderQuantityPriceMap quantityMap : quantityMaps) {
                // Get the product directly from the quantity map
                if (quantityMap.getProduct() != null) {
                    Product product = quantityMap.getProduct();
                    Integer quantity = quantityMap.getQuantity();
                    
                    // Aggregate quantities if the same product appears multiple times
                    productQuantityMap.merge(product, quantity, Integer::sum);
                }
            }
        }
        
        return productQuantityMap;
    }
    
    /**
     * Helper method to upload purchase order attachments to ImgBB.
     * 
     * @param attachments Map of attachments (key: fileName, value: base64 data)
     * @param purchaseOrderId The purchase order ID
     * @throws BadRequestException if ImgBB is not configured or upload fails
     */
    private void uploadPurchaseOrderAttachments(Map<String, String> attachments, Long purchaseOrderId) {
        // Check image location from application properties
        String imageLocation = environment.getProperty("imageLocation");
        
        // Only upload if ImgBB is configured, otherwise skip
        if (!"imgbb".equalsIgnoreCase(imageLocation)) {
            return;
        }
        
        // Get client and validate ImgBB API key
        Client client = clientRepository.findById(getClientId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
        String imgbbApiKey = client.getImgbbApiKey();
        
        if (imgbbApiKey == null || imgbbApiKey.trim().isEmpty()) {
            throw new BadRequestException("ImgBB API key is not configured for this client");
        }
        
        // Get environment name for custom file naming
        String environmentName = environment.getActiveProfiles().length > 0 
            ? environment.getActiveProfiles()[0] 
            : "default";
        
        // Prepare attachment upload requests
        List<ImgbbHelper.AttachmentUploadRequest> uploadRequests = new ArrayList<>();
        for (Map.Entry<String, String> attachment : attachments.entrySet()) {
            String fileName = attachment.getKey();
            String base64Data = attachment.getValue();
            
            if (fileName == null || fileName.trim().isEmpty() || base64Data == null || base64Data.trim().isEmpty()) {
                throw new BadRequestException("Each attachment must have a valid fileName (key) and base64 data (value)");
            }
            
            // No notes field in the new structure
            uploadRequests.add(new ImgbbHelper.AttachmentUploadRequest(fileName, base64Data, null));
        }
        
        // Upload all attachments using ImgbbHelper
        ImgbbHelper imgbbHelper = new ImgbbHelper(imgbbApiKey);
        List<ImgbbHelper.AttachmentUploadResult> uploadResults;
        try {
            uploadResults = imgbbHelper.uploadPurchaseOrderAttachments(
                uploadRequests,
                environmentName,
                client.getName(),
                purchaseOrderId
            );
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload attachments: " + e.getMessage());
        }
        
        // Save resource records to database
        for (ImgbbHelper.AttachmentUploadResult result : uploadResults) {
            Resources resource = new Resources();
            resource.setEntityId(purchaseOrderId);
            resource.setEntityType(EntityType.PURCHASE_ORDER);
            resource.setKey(result.getUrl()); // ImgBB URL in 'key' field
            resource.setValue(result.getDeleteHash()); // Delete hash in 'value' field
            resource.setNotes(result.getNotes());
            
            resourcesRepository.save(resource);
        }
    }
    
    /**
     * Helper method to delete existing purchase order attachments from ImgBB and database.
     * 
     * @param purchaseOrderId The purchase order ID
     */
    private void deleteExistingPurchaseOrderAttachments(Long purchaseOrderId) {
        // Fetch existing resources
        List<Resources> existingResources = resourcesRepository.findByEntityIdAndEntityType(
            purchaseOrderId, 
            EntityType.PURCHASE_ORDER
        );
        
        if (existingResources.isEmpty()) {
            return; // Nothing to delete
        }
        
        // Check if ImgBB is configured
        String imageLocation = environment.getProperty("imageLocation");
        if ("imgbb".equalsIgnoreCase(imageLocation)) {
            // Get client for ImgBB API key
            Client client = clientRepository.findById(getClientId())
                .orElse(null);
            
            if (client != null) {
                String imgbbApiKey = client.getImgbbApiKey();
                
                if (imgbbApiKey != null && !imgbbApiKey.trim().isEmpty()) {
                    ImgbbHelper imgbbHelper = new ImgbbHelper(imgbbApiKey);
                    
                    // Collect delete hashes
                    List<String> deleteHashes = existingResources.stream()
                        .map(Resources::getValue)
                        .filter(hash -> hash != null && !hash.trim().isEmpty())
                        .collect(Collectors.toList());
                    
                    // Delete all from ImgBB
                    imgbbHelper.deleteMultipleImages(deleteHashes);
                }
            }
        }
        
        // Delete all existing resource records from database
        resourcesRepository.deleteAll(existingResources);
    }
    
    /**
     * Updates purchase order quantity price mappings by updating existing records,
     * deleting excess records, or inserting new records as needed.
     * 
     * This approach avoids trigger errors and duplicate entry constraints by:
     * - Updating existing records with new values
     * - Deleting only excess records (if existing > new)
     * - Inserting only additional records (if existing < new)
     * 
     * @param purchaseOrderId The purchase order ID
     * @param request The purchase order request model with new product data
     */
    private void updatePurchaseOrderQuantityPriceMaps(Long purchaseOrderId, PurchaseOrderRequestModel request) {
        // Get existing mappings
        List<PurchaseOrderQuantityPriceMap> existingMappings = 
            purchaseOrderQuantityPriceMapRepository.findByPurchaseOrderId(purchaseOrderId);
        
        // Create new mappings from the request
        List<PurchaseOrderQuantityPriceMap> newMappings = PurchaseOrderQuantityPriceMap.createFromRequest(
            request,
            purchaseOrderId
        );
        
        int existingCount = existingMappings.size();
        int newCount = newMappings.size();
        int minCount = Math.min(existingCount, newCount);
        
        // Step 1: Update existing records with new values (up to minCount)
        for (int i = 0; i < minCount; i++) {
            PurchaseOrderQuantityPriceMap existing = existingMappings.get(i);
            PurchaseOrderQuantityPriceMap newMapping = newMappings.get(i);
            
            // Update the existing record with new values
            existing.setProductId(newMapping.getProductId());
            existing.setQuantity(newMapping.getQuantity());
            existing.setPricePerQuantity(newMapping.getPricePerQuantity());
            
            purchaseOrderQuantityPriceMapRepository.save(existing);
        }
        
        // Step 2: Handle the difference
        if (existingCount > newCount) {
            // Delete excess existing records (keeping at least newCount records)
            List<PurchaseOrderQuantityPriceMap> toDelete = existingMappings.subList(newCount, existingCount);
            purchaseOrderQuantityPriceMapRepository.deleteAll(toDelete);
        } else if (existingCount < newCount) {
            // Insert additional new records
            List<PurchaseOrderQuantityPriceMap> toInsert = newMappings.subList(existingCount, newCount);
            purchaseOrderQuantityPriceMapRepository.saveAll(toInsert);
        }
        // If existingCount == newCount, we've already updated all records in Step 1
    }

    /**
     * Creates multiple purchase orders in a single operation.
     *
     * @param purchaseOrders List of PurchaseOrderRequestModel containing the purchase order data to insert
     * @return BulkInsertResponseModel containing success/failure details for each purchase order
     */
    @Override
    public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreatePurchaseOrders(java.util.List<PurchaseOrderRequestModel> purchaseOrders) {
        if (purchaseOrders == null || purchaseOrders.isEmpty()) {
            throw new BadRequestException("Purchase order list cannot be null or empty");
        }

        com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = 
            new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
        response.setTotalRequested(purchaseOrders.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (PurchaseOrderRequestModel poRequest : purchaseOrders) {
            try {
                createPurchaseOrder(poRequest);
                
                // Find the created purchase order (use vendor number as identifier)
                Optional<PurchaseOrder> createdPO = purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                    poRequest.getPurchaseOrderId(), getClientId());
                if (createdPO.isPresent()) {
                    response.addSuccess(poRequest.getVendorNumber(), createdPO.get().getPurchaseOrderId());
                    successCount++;
                }
            } catch (BadRequestException bre) {
                response.addFailure(
                    poRequest.getVendorNumber() != null ? poRequest.getVendorNumber() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                response.addFailure(
                    poRequest.getVendorNumber() != null ? poRequest.getVendorNumber() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        userLogService.logData(getUserId(), 
            SuccessMessages.PurchaseOrderSuccessMessages.InsertPurchaseOrder + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.PurchaseOrderSubRoute.BULK_CREATE_PURCHASE_ORDER);
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        BulkInsertHelper.createBulkInsertResultMessage(response, "Purchase Order", messageService, getUserId());
        
        return response;
    }
}