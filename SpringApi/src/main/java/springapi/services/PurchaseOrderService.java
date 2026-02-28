package springapi.services;

import com.itextpdf.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import springapi.ErrorMessages;
import springapi.SuccessMessages;
import springapi.authentication.JwtTokenProvider;
import springapi.constants.EntityType;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.filterquerybuilder.PurchaseOrderFilterQueryBuilder;
import springapi.helpers.BulkInsertHelper;
import springapi.helpers.HtmlHelper;
import springapi.helpers.ImgbbHelper;
import springapi.helpers.PdfHelper;
import springapi.models.ApiRoutes;
import springapi.models.databasemodels.Address;
import springapi.models.databasemodels.Client;
import springapi.models.databasemodels.Lead;
import springapi.models.databasemodels.OrderSummary;
import springapi.models.databasemodels.Payment;
import springapi.models.databasemodels.Product;
import springapi.models.databasemodels.PurchaseOrder;
import springapi.models.databasemodels.Resources;
import springapi.models.databasemodels.Shipment;
import springapi.models.databasemodels.ShipmentPackage;
import springapi.models.databasemodels.ShipmentPackageProduct;
import springapi.models.databasemodels.ShipmentProduct;
import springapi.models.databasemodels.User;
import springapi.models.dtos.AddressDuplicateCriteria;
import springapi.models.dtos.PurchaseOrderWithDetails;
import springapi.models.requestmodels.AddressRequestModel;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.PurchaseOrderProductItem;
import springapi.models.requestmodels.PurchaseOrderRequestModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;
import springapi.models.responsemodels.PurchaseOrderResponseModel;
import springapi.repositories.AddressRepository;
import springapi.repositories.ClientRepository;
import springapi.repositories.LeadRepository;
import springapi.repositories.OrderSummaryRepository;
import springapi.repositories.PaymentRepository;
import springapi.repositories.PurchaseOrderRepository;
import springapi.repositories.ResourcesRepository;
import springapi.repositories.ShipmentPackageProductRepository;
import springapi.repositories.ShipmentPackageRepository;
import springapi.repositories.ShipmentProductRepository;
import springapi.repositories.ShipmentRepository;
import springapi.repositories.UserRepository;
import springapi.services.interfaces.PurchaseOrderSubTranslator;

/**
 * Service implementation for PurchaseOrder operations.
 *
 * <p>This service handles all business logic related to purchase order management including CRUD
 * operations, approval workflow, and PDF generation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PurchaseOrderService extends BaseService implements PurchaseOrderSubTranslator {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final AddressRepository addressRepository;
  private final UserRepository userRepository;
  private final LeadRepository leadRepository;
  private final ClientRepository clientRepository;
  private final ResourcesRepository resourcesRepository;
  private final OrderSummaryRepository orderSummaryRepository;
  private final ShipmentRepository shipmentRepository;
  private final ShipmentProductRepository shipmentProductRepository;
  private final ShipmentPackageRepository shipmentPackageRepository;
  private final ShipmentPackageProductRepository shipmentPackageProductRepository;
  private final PaymentRepository paymentRepository;
  private final UserLogService userLogService;
  private final Environment environment;
  private final PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder;
  private final MessageService messageService;

  /** Initializes PurchaseOrderService. */
  @Autowired
  public PurchaseOrderService(
      PurchaseOrderRepository purchaseOrderRepository,
      AddressRepository addressRepository,
      UserRepository userRepository,
      LeadRepository leadRepository,
      ClientRepository clientRepository,
      ResourcesRepository resourcesRepository,
      OrderSummaryRepository orderSummaryRepository,
      ShipmentRepository shipmentRepository,
      ShipmentProductRepository shipmentProductRepository,
      ShipmentPackageRepository shipmentPackageRepository,
      ShipmentPackageProductRepository shipmentPackageProductRepository,
      PaymentRepository paymentRepository,
      UserLogService userLogService,
      PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder,
      MessageService messageService,
      Environment environment,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.addressRepository = addressRepository;
    this.userRepository = userRepository;
    this.leadRepository = leadRepository;
    this.clientRepository = clientRepository;
    this.resourcesRepository = resourcesRepository;
    this.orderSummaryRepository = orderSummaryRepository;
    this.shipmentRepository = shipmentRepository;
    this.shipmentProductRepository = shipmentProductRepository;
    this.shipmentPackageRepository = shipmentPackageRepository;
    this.shipmentPackageProductRepository = shipmentPackageProductRepository;
    this.paymentRepository = paymentRepository;
    this.userLogService = userLogService;
    this.purchaseOrderFilterQueryBuilder = purchaseOrderFilterQueryBuilder;
    this.messageService = messageService;
    this.environment = environment;
  }

  /**
   * Retrieves purchase orders in batches with pagination support.
   *
   * <p>This method returns a paginated list of purchase orders based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * <p>Eagerly loads all related entities including: - OrderSummary (financial breakdown and
   * fulfillment details) - Shipments with Products, Packages, and Courier selections - Created By
   * User - Modified By User - Assigned Lead - Approved By User
   *
   * <p>Advanced Filtering Capabilities: - selectedProductIds: Filter POs containing specific
   * products - address: Filter by shipping address (combined street, city, state, postalCode,
   * country) - All standard PO fields (status, amounts, dates, vendor info, etc.)
   *
   * @param paginationBaseRequestModel The pagination parameters including page size, number,
   *     filters, and sorting
   * @return Paginated response containing purchase order data with all related entities
   * @throws BadRequestException if validation fails
   */
  @Override
  @Transactional(readOnly = true)
  public PaginationBaseResponseModel<PurchaseOrderResponseModel> getPurchaseOrdersInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel) {
    // Valid columns for filtering - includes PO fields and address field
    Set<String> validColumns =
        Set.of(
            // Purchase Order fields
            "purchaseOrderId",
            "vendorNumber",
            "purchaseOrderStatus",
            "priority",
            "expectedDeliveryDate",
            "createdAt",
            "updatedAt",
            "purchaseOrderReceipt",
            "termsConditionsHtml",
            "notes",
            "createdUser",
            "modifiedUser",
            "approvedByUserId",
            "approvedDate",
            "rejectedByUserId",
            "rejectedDate",
            "isDeleted",
            "address");

    // Validate filter conditions if provided
    if (paginationBaseRequestModel.getFilters() != null
        && !paginationBaseRequestModel.getFilters().isEmpty()) {
      for (PaginationBaseRequestModel.FilterCondition filter :
          paginationBaseRequestModel.getFilters()) {
        // Validate column name
        if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.PurchaseOrderErrorMessages.INVALID_COLUMN_NAME,
                  filter.getColumn()));
        }

        // Validate operator
        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.PurchaseOrderErrorMessages.INVALID_OPERATOR, filter.getOperator()));
        }

        // Validate column type matches operator
        String columnType = purchaseOrderFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());
      }
    }

    // Calculate page size and offset
    int start = paginationBaseRequestModel.getStart();
    int end = paginationBaseRequestModel.getEnd();
    int pageSize = end - start;

    // Validate page size
    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_PAGINATION);
    }

    // Create custom Pageable with proper offset handling
    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("purchaseOrderId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // selectedProductIds can be passed as a separate parameter if needed in the future
    // For now, we'll use null to indicate no product filtering
    List<Long> selectedProductIds = null;

    // Single query fetches PO + OrderSummary + Shipments + Products + Packages + PickupLocation
    // (Resources + Payments in 2 extra queries)
    Page<PurchaseOrderWithDetails> page =
        purchaseOrderFilterQueryBuilder.findPaginatedWithDetails(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            selectedProductIds,
            paginationBaseRequestModel.getLogicOperator() != null
                ? paginationBaseRequestModel.getLogicOperator()
                : "AND",
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable);

    List<PurchaseOrderResponseModel> purchaseOrderResponseModels =
        page.getContent().stream().map(PurchaseOrderWithDetails::toResponseModel).toList();

    PaginationBaseResponseModel<PurchaseOrderResponseModel> response =
        new PaginationBaseResponseModel<>();
    response.setData(purchaseOrderResponseModels);
    response.setTotalDataCount(page.getTotalElements());

    return response;
  }

  /**
   * Finds an existing address or creates a new one. Delegates to findOrCreateAddressWithContext.
   * using current security context.
   *
   * @param addressRequest The address request model
   * @return The address ID (existing or newly created)
   */
  private Long findOrCreateAddress(AddressRequestModel addressRequest) {
    return findOrCreateAddressWithContext(addressRequest, getUser(), getClientId());
  }

  @Override
  @Transactional
  public void createPurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
    // Delegate to common method with current security context
    // shouldLog = true for singular operations.
    createPurchaseOrderWithContext(
        purchaseOrderRequestModel, getUser(), getClientId(), getUserId(), true);
  }

  /**
   * Updates an existing purchase order.
   *
   * <p>This method updates an existing purchase order's details including supplier information,
   * OrderSummary (financial breakdown and fulfillment details), and Shipment data (products,
   * packages, courier selections).
   *
   * <p>Flow: 1. Fetch existing PurchaseOrder 2. Update PurchaseOrder with new data 3. Handle
   * Address update if new address data provided 4. Update or create OrderSummary 5. Delete existing
   * Shipments and related data, then create new ones 6. Update attachments
   *
   * @param purchaseOrderRequestModel The purchase order to update
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   */
  @Override
  @Transactional
  public void updatePurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel) {
    // Step 1: Fetch and update the purchase order entity
    PurchaseOrder existingPurchaseOrder =
        purchaseOrderRepository
            .findByPurchaseOrderIdAndClientId(
                purchaseOrderRequestModel.getPurchaseOrderId(), getClientId())
            .orElseThrow(
                () -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID));

    PurchaseOrder updatedPurchaseOrder =
        new PurchaseOrder(purchaseOrderRequestModel, getUser(), existingPurchaseOrder);
    purchaseOrderRepository.save(updatedPurchaseOrder);

    // Step 2: Handle Address - find existing duplicate or create new
    Long entityAddressId =
        findOrCreateAddress(purchaseOrderRequestModel.getOrderSummary().getAddress());

    // Step 3: Update or create OrderSummary
    Optional<OrderSummary> existingOrderSummaryOpt =
        orderSummaryRepository.findByEntityTypeAndEntityId(
            OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
            updatedPurchaseOrder.getPurchaseOrderId());

    OrderSummary orderSummary;
    if (existingOrderSummaryOpt.isPresent()) {
      // Update existing OrderSummary
      OrderSummary existingOrderSummary = existingOrderSummaryOpt.get();
      orderSummary =
          new OrderSummary(
              OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
              updatedPurchaseOrder.getPurchaseOrderId(),
              purchaseOrderRequestModel.getOrderSummary(), // orderSummaryData
              entityAddressId,
              getUser(),
              existingOrderSummary);

      // Recalculate pendingAmount based on payments made
      // When updating, if grandTotal changes, we need to recalculate: pendingAmount = newGrandTotal
      // - totalPaid
      Long totalPaidPaise =
          paymentRepository.getTotalNetPaidPaiseForEntity(
              Payment.EntityType.PURCHASE_ORDER.getValue(),
              updatedPurchaseOrder.getPurchaseOrderId());
      BigDecimal totalPaid = BigDecimal.valueOf(totalPaidPaise).divide(BigDecimal.valueOf(100));
      BigDecimal newPendingAmount = orderSummary.getGrandTotal().subtract(totalPaid);

      // Ensure pendingAmount doesn't go negative (shouldn't happen, but safety check)
      if (newPendingAmount.compareTo(BigDecimal.ZERO) < 0) {
        newPendingAmount = BigDecimal.ZERO;
      }

      orderSummary.setPendingAmount(newPendingAmount);
    } else {
      // Create new OrderSummary
      orderSummary =
          new OrderSummary(
              OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
              updatedPurchaseOrder.getPurchaseOrderId(),
              purchaseOrderRequestModel.getOrderSummary(), // orderSummaryData
              entityAddressId,
              getClientId(),
              getUser());
      // For new OrderSummary, pendingAmount is already set to grandTotal in constructor
    }

    orderSummary = orderSummaryRepository.save(orderSummary);

    // Build a canonical map of productId -> pricePerUnit from request.products[]
    // We always persist custom PO pricing from request (no fallback to Product default pricing).
    final Map<Long, BigDecimal> productPriceMap =
        buildProductIdToPricePerUnitMap(purchaseOrderRequestModel);

    // Step 5: Delete existing shipments and related data, then create new ones
    List<Shipment> existingShipments =
        shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
    for (Shipment existingShipment : existingShipments) {
      // Delete ShipmentPackageProducts (cascade will handle ShipmentPackages and ShipmentProducts)
      List<ShipmentPackage> existingPackages =
          shipmentPackageRepository.findByShipmentId(existingShipment.getShipmentId());
      for (ShipmentPackage existingPackage : existingPackages) {
        shipmentPackageProductRepository.deleteByShipmentPackageId(
            existingPackage.getShipmentPackageId());
      }
      shipmentPackageRepository.deleteByShipmentId(existingShipment.getShipmentId());
      shipmentProductRepository.deleteByShipmentId(existingShipment.getShipmentId());
    }
    shipmentRepository.deleteAll(existingShipments);

    // Create new Shipments with all related data
    if (purchaseOrderRequestModel.getShipments() != null
        && !purchaseOrderRequestModel.getShipments().isEmpty()) {
      for (PurchaseOrderRequestModel.ShipmentData shipmentData :
          purchaseOrderRequestModel.getShipments()) {
        // Create Shipment
        Shipment shipment =
            new Shipment(orderSummary.getOrderSummaryId(), shipmentData, getClientId(), getUser());

        // Step 5.2: Set courier selection (required for each shipment)
        if (shipmentData.getSelectedCourier() != null) {
          shipment.setCourierSelection(shipmentData.getSelectedCourier());
        } else {
          throw new BadRequestException(
              ErrorMessages.ShipmentErrorMessages.COURIER_SELECTION_REQUIRED);
        }

        shipment = shipmentRepository.save(shipment);

        // Step 5.3: Create ShipmentProducts
        if (shipmentData.getProducts() != null && !shipmentData.getProducts().isEmpty()) {
          List<ShipmentProduct> shipmentProducts = new ArrayList<>();
          for (PurchaseOrderRequestModel.ShipmentProductData productData :
              shipmentData.getProducts()) {
            // Ensure allocatedPrice always matches PO-level custom price from request.products[]
            BigDecimal canonicalPrice = productPriceMap.get(productData.getProductId());
            if (canonicalPrice == null) {
              throw new BadRequestException(
                  "Allocated price not found for productId "
                      + productData.getProductId()
                      + ". Ensure request.products[] includes this product with pricePerUnit.");
            }
            productData.setAllocatedPrice(canonicalPrice);
            ShipmentProduct shipmentProduct =
                new ShipmentProduct(shipment.getShipmentId(), productData);
            shipmentProducts.add(shipmentProduct);
          }
          shipmentProductRepository.saveAll(shipmentProducts);
        }

        // Step 5.4: Create ShipmentPackages with ShipmentPackageProducts
        if (shipmentData.getPackages() != null && !shipmentData.getPackages().isEmpty()) {
          for (PurchaseOrderRequestModel.ShipmentPackageData packageData :
              shipmentData.getPackages()) {
            ShipmentPackage shipmentPackage =
                new ShipmentPackage(shipment.getShipmentId(), packageData);
            shipmentPackage = shipmentPackageRepository.save(shipmentPackage);

            // Step 5.5: Create ShipmentPackageProducts
            if (packageData.getProducts() != null && !packageData.getProducts().isEmpty()) {
              List<ShipmentPackageProduct> packageProducts = new ArrayList<>();
              for (PurchaseOrderRequestModel.PackageProductData productData :
                  packageData.getProducts()) {
                ShipmentPackageProduct packageProduct =
                    new ShipmentPackageProduct(shipmentPackage.getShipmentPackageId(), productData);
                packageProducts.add(packageProduct);
              }
              shipmentPackageProductRepository.saveAll(packageProducts);
            } else {
              throw new BadRequestException(
                  ErrorMessages.ShipmentPackageProductErrorMessages.AT_LEAST_ONE_PRODUCT_REQUIRED);
            }
          }
        } else {
          throw new BadRequestException(
              ErrorMessages.ShipmentPackageErrorMessages.AT_LEAST_ONE_PACKAGE_REQUIRED);
        }
      }
    } else {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.AT_LEAST_ONE_SHIPMENT_REQUIRED);
    }

    // Step 6: Update the Resources (attachments)
    // Delete all existing resources from ImgBB and database, then create new ones
    deleteExistingPurchaseOrderAttachments(updatedPurchaseOrder.getPurchaseOrderId());

    // Create new resources if attachments are provided
    if (purchaseOrderRequestModel.getAttachments() != null
        && !purchaseOrderRequestModel.getAttachments().isEmpty()) {

      uploadPurchaseOrderAttachments(
          purchaseOrderRequestModel.getAttachments(), updatedPurchaseOrder.getPurchaseOrderId());
    }

    // Log the update
    userLogService.logData(
        getUserId(),
        SuccessMessages.PurchaseOrderSuccessMessages.UPDATE_PURCHASE_ORDER
            + " "
            + updatedPurchaseOrder.getPurchaseOrderId(),
        ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER);
  }

  /**
   * Retrieves detailed information about a specific purchase order by ID.
   *
   * <p>This method returns comprehensive purchase order details including all related entities: -
   * OrderSummary (financial breakdown and fulfillment details) - Shipments with Products, Packages,
   * and Courier selections - Created By User - Modified By User - Assigned Lead - Approved By User
   *
   * @param id The ID of the purchase order to retrieve
   * @return The purchase order details with all relationships
   * @throws NotFoundException if the purchase order is not found or doesn't belong to the current
   *     client
   */
  @Override
  @Transactional(readOnly = true)
  public PurchaseOrderResponseModel getPurchaseOrderDetailsById(long id) {
    // Fetch purchase order with all relationships and validate it belongs to current client
    Optional<PurchaseOrder> purchaseOrderOptional =
        purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(id, getClientId());

    if (purchaseOrderOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID);
    }

    PurchaseOrder purchaseOrder = purchaseOrderOptional.get();

    // Load resources (attachments) for this purchase order filtered by entityType
    List<Resources> resources =
        resourcesRepository.findByEntityIdAndEntityType(id, EntityType.PURCHASE_ORDER);
    purchaseOrder.setAttachments(resources);

    // Load OrderSummary with shipments and related entities
    OrderSummary orderSummary = null;
    Optional<OrderSummary> orderSummaryOptional =
        orderSummaryRepository.findByEntityTypeAndEntityId(
            OrderSummary.EntityType.PURCHASE_ORDER.getValue(), id);

    if (orderSummaryOptional.isPresent()) {
      orderSummary = orderSummaryOptional.get();

      // Load shipments for this OrderSummary
      List<Shipment> shipments =
          shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
      orderSummary.setShipments(shipments);

      // Load related entities for each shipment (products, packages, package products)
      for (Shipment shipment : shipments) {
        // Initialize PickupLocation and its Address for each shipment (required for response model)
        if (shipment.getPickupLocation() != null) {
          org.hibernate.Hibernate.initialize(shipment.getPickupLocation());
          // Initialize the address within the pickup location
          if (shipment.getPickupLocation().getAddress() != null) {
            org.hibernate.Hibernate.initialize(shipment.getPickupLocation().getAddress());
          }
        }

        // Load shipment products
        List<ShipmentProduct> shipmentProducts =
            shipmentProductRepository.findByShipmentId(shipment.getShipmentId());
        shipment.setShipmentProducts(shipmentProducts);

        // Initialize Product entities for shipment products
        for (ShipmentProduct shipmentProduct : shipmentProducts) {
          if (shipmentProduct.getProduct() != null) {
            org.hibernate.Hibernate.initialize(shipmentProduct.getProduct());
          }
        }

        // Load shipment packages
        List<ShipmentPackage> shipmentPackages =
            shipmentPackageRepository.findByShipmentId(shipment.getShipmentId());
        shipment.setShipmentPackages(shipmentPackages);

        // Load package products for each shipment package
        for (ShipmentPackage shipmentPackage : shipmentPackages) {
          // Initialize Package entity
          if (shipmentPackage.getPackageInfo() != null) {
            org.hibernate.Hibernate.initialize(shipmentPackage.getPackageInfo());
          }

          List<ShipmentPackageProduct> packageProducts =
              shipmentPackageProductRepository.findByShipmentPackageId(
                  shipmentPackage.getShipmentPackageId());
          shipmentPackage.setShipmentPackageProducts(packageProducts);

          // Initialize Product entities for package products
          for (ShipmentPackageProduct packageProduct : packageProducts) {
            if (packageProduct.getProduct() != null) {
              org.hibernate.Hibernate.initialize(packageProduct.getProduct());
            }
          }
        }
      }
    }

    // Load payments for this purchase order
    List<Payment> payments =
        paymentRepository.findAllByPurchaseOrderId(purchaseOrder.getPurchaseOrderId());

    // Convert to response model (constructor handles all mapping)
    PurchaseOrderResponseModel responseModel =
        new PurchaseOrderResponseModel(purchaseOrder, orderSummary);

    // Add payments to response model
    if (payments != null && !payments.isEmpty()) {
      for (Payment payment : payments) {
        responseModel
            .getPayments()
            .add(new springapi.models.responsemodels.PaymentResponseModel(payment));
      }
    }

    return responseModel;
  }

  /**
   * Toggles the deleted status of a purchase order (soft delete/restore).
   *
   * <p>This method toggles the deleted flag of a purchase order without permanently removing it
   * from the database. Deleted purchase orders are hidden from standard queries.
   *
   * @param id The ID of the purchase order to toggle
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found or doesn't belong to the current
   *     client
   */
  @Override
  @Transactional
  public void togglePurchaseOrder(long id) {
    // Validate purchase order exists and belongs to current client
    Optional<PurchaseOrder> purchaseOrderOptional =
        purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
    if (purchaseOrderOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID);
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
        SuccessMessages.PurchaseOrderSuccessMessages.TOGGLE_PURCHASE_ORDER
            + " "
            + purchaseOrder.getPurchaseOrderId(),
        ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER);
  }

  /**
   * Approves a purchase order.
   *
   * <p>This method marks a purchase order as approved by setting the approvedByUserId to the
   * current user's ID, allowing it to proceed to the next stage in the procurement workflow.
   *
   * @param id The ID of the purchase order to approve
   * @throws NotFoundException if the purchase order is not found or doesn't belong to the current
   *     client
   * @throws BadRequestException if the purchase order is already approved
   */
  @Override
  @Transactional
  public void approvedByPurchaseOrder(long id) {
    // Validate purchase order exists and belongs to current client
    Optional<PurchaseOrder> purchaseOrderOptional =
        purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
    if (purchaseOrderOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID);
    }

    PurchaseOrder purchaseOrder = purchaseOrderOptional.get();

    // Check if purchase order is already approved
    if (purchaseOrder.getApprovedByUserId() != null) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.ALREADY_APPROVED);
    }

    // Set approval fields (also clears rejection fields and updates modified user)
    purchaseOrder.setApprovalFields(getUser(), getUserId());

    // Save the updated purchase order
    purchaseOrderRepository.save(purchaseOrder);

    // Logging
    userLogService.logData(
        getUserId(),
        SuccessMessages.PurchaseOrderSuccessMessages.SET_APPROVED_BY_PURCHASE_ORDER
            + " PO: "
            + purchaseOrder.getPurchaseOrderId(),
        ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER);
  }

  /**
   * Rejects a purchase order.
   *
   * <p>This method marks a purchase order as rejected by setting the rejectedByUserId to the
   * current user's ID, preventing it from proceeding in the procurement workflow.
   *
   * @param id The ID of the purchase order to reject
   * @throws NotFoundException if the purchase order is not found or doesn't belong to the current
   *     client
   * @throws BadRequestException if the purchase order is already rejected
   */
  @Override
  @Transactional
  public void rejectedByPurchaseOrder(long id) {
    // Validate purchase order exists and belongs to current client
    Optional<PurchaseOrder> purchaseOrderOptional =
        purchaseOrderRepository.findByPurchaseOrderIdAndClientId(id, getClientId());
    if (purchaseOrderOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID);
    }

    PurchaseOrder purchaseOrder = purchaseOrderOptional.get();

    // Check if purchase order is already rejected
    if (purchaseOrder.getRejectedByUserId() != null) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.ALREADY_REJECTED);
    }

    // Set rejection fields (also clears approval fields and updates modified user)
    purchaseOrder.setRejectionFields(getUser(), getUserId());

    // Save the updated purchase order
    purchaseOrderRepository.save(purchaseOrder);

    // Logging
    userLogService.logData(
        getUserId(),
        SuccessMessages.PurchaseOrderSuccessMessages.SET_REJECTED_BY_PURCHASE_ORDER
            + " PO: "
            + purchaseOrder.getPurchaseOrderId(),
        ApiRoutes.PurchaseOrderSubRoute.REJECTED_BY_PURCHASE_ORDER);
  }

  /**
   * Generates a PDF document for a purchase order.
   *
   * <p>This method generates a formatted PDF document containing all purchase order details
   * including supplier information, line items, totals, and terms.
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
  @Transactional
  public byte[] getPurchaseOrderPdf(long id)
      throws TemplateException, IOException, DocumentException {
    // Fetch purchase order with all relationships
    Optional<PurchaseOrder> purchaseOrderOptional =
        purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(id, getClientId());

    if (purchaseOrderOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID);
    }

    PurchaseOrder purchaseOrder = purchaseOrderOptional.get();

    // Fetch OrderSummary to get shipping address
    OrderSummary orderSummary =
        orderSummaryRepository
            .findByEntityTypeAndEntityId(
                OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                purchaseOrder.getPurchaseOrderId())
            .orElseThrow(
                () -> new NotFoundException(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND));

    // Fetch shipping address from OrderSummary
    Optional<Address> shippingAddressOptional =
        addressRepository.findById(orderSummary.getEntityAddressId());

    if (shippingAddressOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.INVALID_ID);
    }

    final Address shippingAddress = shippingAddressOptional.get();

    // Fetch created by user
    Optional<User> purchaseOrderCreatedByOptional =
        userRepository.findByUserIdAndClientId(getUserId(), getClientId());

    if (purchaseOrderCreatedByOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    final User purchaseOrderCreatedBy = purchaseOrderCreatedByOptional.get();

    // Fetch approved by user (if approved)
    User purchaseOrderApprovedBy = null;
    if (purchaseOrder.getApprovedByUserId() != null) {
      Optional<User> purchaseOrderApprovedByOptional =
          userRepository.findByUserIdAndClientId(
              purchaseOrder.getApprovedByUserId(), getClientId());

      if (purchaseOrderApprovedByOptional.isEmpty()) {
        throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
      }

      purchaseOrderApprovedBy = purchaseOrderApprovedByOptional.get();
    }

    // Fetch lead
    Lead lead =
        leadRepository.findLeadWithDetailsByIdIncludingDeleted(
            purchaseOrder.getAssignedLeadId(), getClientId());

    if (lead == null) {
      throw new NotFoundException(ErrorMessages.LeadsErrorMessages.INVALID_ID);
    }

    // Fetch client details
    Optional<Client> clientOptional = clientRepository.findById(getClientId());

    if (clientOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID);
    }

    Client client = clientOptional.get();

    // Get product quantity map
    Map<Product, Integer> productQuantityMap = getProductQuantityMap(purchaseOrder);

    // Generate HTML from template
    String htmlContent =
        formPurchaseOrderPdf(
            client,
            purchaseOrder,
            shippingAddress,
            purchaseOrderCreatedBy,
            purchaseOrderApprovedBy,
            lead,
            productQuantityMap);

    // Replace br tags for PDF compatibility
    htmlContent = HtmlHelper.replaceBrTags(htmlContent);

    // Convert HTML to PDF
    byte[] pdfBytes = PdfHelper.convertPurchaseOrderHtmlToPdf(htmlContent);

    // Log the PDF generation
    userLogService.logData(
        getUserId(),
        SuccessMessages.PurchaseOrderSuccessMessages.GET_PURCHASE_ORDER_PDF + " " + id,
        ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_PDF);

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
      Map<Product, Integer> productQuantityMap)
      throws IOException, TemplateException {

    // Configure FreeMarker
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setClassLoaderForTemplateLoading(
        Thread.currentThread().getContextClassLoader(), "InvoiceTemplates");

    // Load template
    final Template template = cfg.getTemplate("PurchaseOrder.ftl");

    // Prepare template data
    Map<String, Object> templateData = new HashMap<>();

    // Company information
    templateData.put("companyName", client.getName());
    templateData.put("website", client.getWebsite() != null ? client.getWebsite() : "");
    templateData.put(
        "fullAddress",
        client.getSendGridEmailAddress() != null
            ? client.getSendGridEmailAddress()
            : client.getSupportEmail());
    templateData.put(
        "supportEmail", client.getSupportEmail() != null ? client.getSupportEmail() : "");

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
   * Retrieves the product to quantity mapping for a purchase order. Gets products from.
   * ShipmentProduct via OrderSummary.
   *
   * @param purchaseOrder The purchase order entity
   * @return Map of Product to quantity
   */
  private Map<Product, Integer> getProductQuantityMap(PurchaseOrder purchaseOrder) {
    Map<Product, Integer> productQuantityMap = new LinkedHashMap<>();

    // Get OrderSummary for this purchase order
    Optional<OrderSummary> orderSummaryOpt =
        orderSummaryRepository.findByEntityTypeAndEntityId(
            OrderSummary.EntityType.PURCHASE_ORDER.getValue(), purchaseOrder.getPurchaseOrderId());

    if (orderSummaryOpt.isPresent()) {
      OrderSummary orderSummary = orderSummaryOpt.get();

      // Get all shipments for this order summary
      List<Shipment> shipments =
          shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());

      // Get all shipment products
      for (Shipment shipment : shipments) {
        List<ShipmentProduct> shipmentProducts =
            shipmentProductRepository.findByShipmentId(shipment.getShipmentId());

        for (ShipmentProduct shipmentProduct : shipmentProducts) {
          // Fetch product details
          Product product = shipmentProduct.getProduct();
          if (product != null) {
            Integer quantity = shipmentProduct.getAllocatedQuantity();

            // Aggregate quantities if the same product appears multiple times
            productQuantityMap.merge(product, quantity, Integer::sum);
          }
        }
      }
    }

    return productQuantityMap;
  }

  /**
   * Helper method to upload purchase order attachments to ImgBB. Delegates to.
   * uploadPurchaseOrderAttachmentsWithContext using current security context.
   *
   * @param attachments Map of attachments (key: fileName, value: base64 data)
   * @param purchaseOrderId The purchase order ID
   * @throws BadRequestException if ImgBB is not configured or upload fails
   */
  private void uploadPurchaseOrderAttachments(
      Map<String, String> attachments, Long purchaseOrderId) {
    uploadPurchaseOrderAttachmentsWithContext(attachments, purchaseOrderId, getClientId());
  }

  /**
   * Helper method to delete existing purchase order attachments from ImgBB and database.
   *
   * @param purchaseOrderId The purchase order ID
   */
  private void deleteExistingPurchaseOrderAttachments(Long purchaseOrderId) {
    // Fetch existing resources
    List<Resources> existingResources =
        resourcesRepository.findByEntityIdAndEntityType(purchaseOrderId, EntityType.PURCHASE_ORDER);

    if (existingResources.isEmpty()) {
      return; // Nothing to delete
    }

    // Check if ImgBB is configured
    String imageLocation = environment.getProperty("imageLocation");
    if ("imgbb".equalsIgnoreCase(imageLocation)) {
      // Get client for ImgBB API key
      Client client = clientRepository.findById(getClientId()).orElse(null);

      if (client != null) {
        String imgbbApiKey = client.getImgbbApiKey();

        if (imgbbApiKey != null && !imgbbApiKey.trim().isEmpty()) {
          ImgbbHelper imgbbHelper = new ImgbbHelper(imgbbApiKey);

          // Collect delete hashes
          List<String> deleteHashes =
              existingResources.stream()
                  .map(Resources::getDeleteHashValue)
                  .filter(hash -> hash != null && !hash.trim().isEmpty())
                  .collect(Collectors.toCollection(ArrayList::new));

          // Delete all from ImgBB
          imgbbHelper.deleteMultipleImages(deleteHashes);
        }
      }
    }

    // Delete all existing resource records from database
    resourcesRepository.deleteAll(existingResources);
  }

  /**
   * Creates multiple purchase orders asynchronously with explicit security context.
   *
   * <p>This method runs in a separate thread using @Async annotation and processes each purchase
   * order individually. Results are sent via message to the requesting user. Uses
   * Propagation.NOT_SUPPORTED to prevent rollback-only issues.
   *
   * @param purchaseOrders List of PurchaseOrderRequestModel containing the purchase order data to
   *     insert
   * @param requestingUserId The user ID of the user making the request (captured from security
   *     context)
   * @param requestingUserLoginName The login name of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  @Override
  @Async
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void bulkCreatePurchaseOrdersAsync(
      java.util.List<PurchaseOrderRequestModel> purchaseOrders,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId) {
    try {
      // Validate input
      if (purchaseOrders == null || purchaseOrders.isEmpty()) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Purchase order"));
      }

      springapi.models.responsemodels.BulkInsertResponseModel<Long> response =
          new springapi.models.responsemodels.BulkInsertResponseModel<>();
      response.setTotalRequested(purchaseOrders.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each purchase order individually
      for (PurchaseOrderRequestModel poRequest : purchaseOrders) {
        try {
          // Call createPurchaseOrderWithContext with explicit security context (shouldLog = false
          // for bulk)
          Long createdPurchaseOrderId =
              createPurchaseOrderWithContext(
                  poRequest, requestingUserLoginName, requestingClientId, requestingUserId, false);
          String identifier =
              poRequest.getVendorNumber() != null
                  ? poRequest.getVendorNumber()
                  : "PO-" + createdPurchaseOrderId;
          response.addSuccess(identifier, createdPurchaseOrderId);
          successCount++;

        } catch (BadRequestException bre) {
          // Validation or business logic error
          response.addFailure(
              poRequest.getVendorNumber() != null ? poRequest.getVendorNumber() : "unknown",
              bre.getMessage());
          failureCount++;
        } catch (Exception e) {
          // Unexpected error
          response.addFailure(
              poRequest.getVendorNumber() != null ? poRequest.getVendorNumber() : "unknown",
              "Error: " + e.getMessage());
          failureCount++;
        }
      }

      // Log bulk purchase order creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.PurchaseOrderSuccessMessages.INSERT_PURCHASE_ORDER
              + " (Bulk: "
              + successCount
              + " succeeded, "
              + failureCount
              + " failed)",
          ApiRoutes.PurchaseOrderSubRoute.BULK_CREATE_PURCHASE_ORDER);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the helper (using captured context)
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          response,
          new BulkInsertHelper.BulkMessageTemplate(
              "Purchase Order", "Purchase Orders", "Vendor Number", "Purchase Order ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));

    } catch (Exception e) {
      // Still send a message to user about the failure (using captured userId)
      springapi.models.responsemodels.BulkInsertResponseModel<Long> errorResponse =
          new springapi.models.responsemodels.BulkInsertResponseModel<>();
      errorResponse.setTotalRequested(purchaseOrders != null ? purchaseOrders.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(purchaseOrders != null ? purchaseOrders.size() : 0);
      errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          errorResponse,
          new BulkInsertHelper.BulkMessageTemplate(
              "Purchase Order", "Purchase Orders", "Vendor Number", "Purchase Order ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));
    }
  }

  /**
   * Creates a purchase order with explicit security context parameters. This is the common method.
   * called by both singular and bulk insert operations. Used by async bulk import where security
   * context is not available in the worker thread.
   *
   * @param purchaseOrderRequestModel The purchase order to create
   * @param createdUser The login name of the user creating the purchase order
   * @param clientId The client ID for the purchase order
   * @param userId The user ID creating the purchase order
   * @param shouldLog Whether to log this individual operation (false for bulk operations)
   * @return The ID of the created purchase order
   */
  @Transactional
  protected Long createPurchaseOrderWithContext(
      PurchaseOrderRequestModel purchaseOrderRequestModel,
      String createdUser,
      Long clientId,
      Long userId,
      boolean shouldLog) {
    // Step 1: Create the purchase order entity (validations are done in constructor, including
    // OrderSummary check)
    PurchaseOrder purchaseOrder =
        new PurchaseOrder(purchaseOrderRequestModel, createdUser, clientId);

    // Step 2: Handle Address - find existing duplicate or create new
    Long entityAddressId =
        findOrCreateAddressWithContext(
            purchaseOrderRequestModel.getOrderSummary().getAddress(), createdUser, clientId);

    // Step 3: Save the purchase order to database (to get purchaseOrderId)
    purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

    // Step 4: Create OrderSummary entity
    OrderSummary orderSummary =
        new OrderSummary(
            OrderSummary.EntityType.PURCHASE_ORDER.getValue(), // entityType
            purchaseOrder.getPurchaseOrderId(), // entityId
            purchaseOrderRequestModel.getOrderSummary(), // orderSummaryData
            entityAddressId,
            clientId,
            createdUser);

    orderSummary = orderSummaryRepository.save(orderSummary);

    // Build a canonical map of productId -> pricePerUnit from request.products[]
    // We always persist custom PO pricing from request (no fallback to Product default pricing).
    final Map<Long, BigDecimal> productPriceMap =
        buildProductIdToPricePerUnitMap(purchaseOrderRequestModel);

    // Step 5: Create Shipments with all related data
    if (purchaseOrderRequestModel.getShipments() != null
        && !purchaseOrderRequestModel.getShipments().isEmpty()) {
      for (PurchaseOrderRequestModel.ShipmentData shipmentData :
          purchaseOrderRequestModel.getShipments()) {
        // Step 5.1: Create Shipment
        Shipment shipment =
            new Shipment(orderSummary.getOrderSummaryId(), shipmentData, clientId, createdUser);

        // Step 5.2: Set courier selection (required for each shipment)
        if (shipmentData.getSelectedCourier() != null) {
          shipment.setCourierSelection(shipmentData.getSelectedCourier());
        } else {
          throw new BadRequestException(
              ErrorMessages.ShipmentErrorMessages.COURIER_SELECTION_REQUIRED);
        }

        shipment = shipmentRepository.save(shipment);

        // Step 5.3: Create ShipmentProducts
        if (shipmentData.getProducts() != null && !shipmentData.getProducts().isEmpty()) {
          List<ShipmentProduct> shipmentProducts = new ArrayList<>();
          for (PurchaseOrderRequestModel.ShipmentProductData productData :
              shipmentData.getProducts()) {
            // Ensure allocatedPrice always matches PO-level custom price from request.products[]
            BigDecimal canonicalPrice = productPriceMap.get(productData.getProductId());
            if (canonicalPrice == null) {
              throw new BadRequestException(
                  "Allocated price not found for productId "
                      + productData.getProductId()
                      + ". Ensure request.products[] includes this product with pricePerUnit.");
            }
            productData.setAllocatedPrice(canonicalPrice);
            ShipmentProduct shipmentProduct =
                new ShipmentProduct(shipment.getShipmentId(), productData);
            shipmentProducts.add(shipmentProduct);
          }
          shipmentProductRepository.saveAll(shipmentProducts);
        }

        // Step 5.4: Create ShipmentPackages with ShipmentPackageProducts
        if (shipmentData.getPackages() != null && !shipmentData.getPackages().isEmpty()) {
          for (PurchaseOrderRequestModel.ShipmentPackageData packageData :
              shipmentData.getPackages()) {
            ShipmentPackage shipmentPackage =
                new ShipmentPackage(shipment.getShipmentId(), packageData);
            shipmentPackage = shipmentPackageRepository.save(shipmentPackage);

            // Step 5.5: Create ShipmentPackageProducts
            if (packageData.getProducts() != null && !packageData.getProducts().isEmpty()) {
              List<ShipmentPackageProduct> packageProducts = new ArrayList<>();
              for (PurchaseOrderRequestModel.PackageProductData productData :
                  packageData.getProducts()) {
                ShipmentPackageProduct packageProduct =
                    new ShipmentPackageProduct(shipmentPackage.getShipmentPackageId(), productData);
                packageProducts.add(packageProduct);
              }
              shipmentPackageProductRepository.saveAll(packageProducts);
            } else {
              throw new BadRequestException(
                  ErrorMessages.ShipmentPackageProductErrorMessages.AT_LEAST_ONE_PRODUCT_REQUIRED);
            }
          }
        } else {
          throw new BadRequestException(
              ErrorMessages.ShipmentPackageErrorMessages.AT_LEAST_ONE_PACKAGE_REQUIRED);
        }
      }
    } else {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.AT_LEAST_ONE_SHIPMENT_REQUIRED);
    }

    // Step 6: Handle attachments if provided
    if (purchaseOrderRequestModel.getAttachments() != null
        && !purchaseOrderRequestModel.getAttachments().isEmpty()) {

      uploadPurchaseOrderAttachmentsWithContext(
          purchaseOrderRequestModel.getAttachments(), purchaseOrder.getPurchaseOrderId(), clientId);
    }

    // Log the creation if required
    if (shouldLog) {
      userLogService.logDataWithContext(
          userId,
          createdUser,
          clientId,
          SuccessMessages.PurchaseOrderSuccessMessages.INSERT_PURCHASE_ORDER
              + " "
              + purchaseOrder.getPurchaseOrderId()
              + " with OrderSummary "
              + orderSummary.getOrderSummaryId()
              + " and "
              + (purchaseOrderRequestModel.getShipments() != null
                  ? purchaseOrderRequestModel.getShipments().size()
                  : 0)
              + " shipments",
          ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER);
    }

    return purchaseOrder.getPurchaseOrderId();
  }

  /**
   * Build a canonical map of productId -> pricePerUnit from request.products[].
   *
   * <p>Business rule: PO always uses custom per-unit pricing from the request payload. This may
   * equal the product default price, but we never fall back to the Product table when persisting PO
   * shipment product pricing.
   */
  private Map<Long, BigDecimal> buildProductIdToPricePerUnitMap(
      PurchaseOrderRequestModel purchaseOrderRequestModel) {
    if (purchaseOrderRequestModel == null) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_REQUEST);
    }

    if (purchaseOrderRequestModel.getProducts() == null
        || purchaseOrderRequestModel.getProducts().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.AT_LEAST_ONE_PRODUCT_REQUIRED);
    }

    Map<Long, BigDecimal> map = new HashMap<>();
    for (PurchaseOrderProductItem item : purchaseOrderRequestModel.getProducts()) {
      if (item == null) {
        continue;
      }

      if (item.getProductId() == null || item.getProductId() <= 0) {
        throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_ID);
      }

      if (item.getPricePerUnit() == null) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.PurchaseOrderErrorMessages.PRICE_PER_UNIT_REQUIRED_FOR_PRODUCT_FORMAT,
                item.getProductId()));
      }
      if (item.getPricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.PurchaseOrderErrorMessages
                    .PRICE_PER_UNIT_MUST_BE_NON_NEGATIVE_FOR_PRODUCT_FORMAT,
                item.getProductId()));
      }

      // Enforce uniqueness to avoid ambiguous pricing
      if (map.containsKey(item.getProductId())) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.PurchaseOrderErrorMessages.DUPLICATE_PRODUCT_ID_FORMAT,
                item.getProductId()));
      }

      map.put(item.getProductId(), item.getPricePerUnit());
    }

    return map;
  }

  /**
   * Finds an existing address or creates a new one with explicit security context. Used by async.
   * bulk import where security context is not available.
   *
   * @param addressRequest The address request model
   * @param createdUser The login name of the user creating the address
   * @param clientId The client ID for the address
   * @return The address ID (existing or newly created)
   */
  private Long findOrCreateAddressWithContext(
      AddressRequestModel addressRequest, String createdUser, Long clientId) {
    if (addressRequest == null) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.ADDRESS_DATA_REQUIRED);
    }

    Long effectiveClientId =
        addressRequest.getClientId() != null ? addressRequest.getClientId() : clientId;

    // Check for exact duplicate (normalization is handled in the repository query)
    AddressDuplicateCriteria duplicateCriteria = new AddressDuplicateCriteria();
    duplicateCriteria.setUserId(addressRequest.getUserId());
    duplicateCriteria.setClientId(effectiveClientId);
    duplicateCriteria.setAddressType(addressRequest.getAddressType());
    duplicateCriteria.setStreetAddress(addressRequest.getStreetAddress());
    duplicateCriteria.setStreetAddress2(addressRequest.getStreetAddress2());
    duplicateCriteria.setStreetAddress3(addressRequest.getStreetAddress3());
    duplicateCriteria.setCity(addressRequest.getCity());
    duplicateCriteria.setState(addressRequest.getState());
    duplicateCriteria.setPostalCode(addressRequest.getPostalCode());
    duplicateCriteria.setNameOnAddress(addressRequest.getNameOnAddress());
    duplicateCriteria.setEmailOnAddress(addressRequest.getEmailOnAddress());
    duplicateCriteria.setPhoneOnAddress(addressRequest.getPhoneOnAddress());
    duplicateCriteria.setCountry(addressRequest.getCountry());
    duplicateCriteria.setIsPrimary(addressRequest.getIsPrimary());
    duplicateCriteria.setIsDeleted(addressRequest.getIsDeleted());

    Optional<Address> existingAddress = addressRepository.findExactDuplicate(duplicateCriteria);

    if (existingAddress.isPresent()) {
      return existingAddress.get().getAddressId();
    }

    // No duplicate found, create new address
    if (addressRequest.getClientId() == null) {
      addressRequest.setClientId(clientId);
    }

    Address newAddress = new Address(addressRequest, createdUser);
    newAddress = addressRepository.save(newAddress);
    return newAddress.getAddressId();
  }

  /**
   * Helper method to upload purchase order attachments to ImgBB with explicit context. Used by.
   * async bulk import where security context is not available.
   *
   * @param attachments Map of attachments (key: fileName, value: base64 data)
   * @param purchaseOrderId The purchase order ID
   * @param clientId The client ID
   * @throws BadRequestException if ImgBB is not configured or upload fails
   */
  private void uploadPurchaseOrderAttachmentsWithContext(
      Map<String, String> attachments, Long purchaseOrderId, Long clientId) {
    if (attachments == null || attachments.isEmpty()) {
      return; // No attachments to process
    }

    // Check image location from application properties
    String imageLocation = environment.getProperty("imageLocation");
    final boolean isImgbbConfigured = "imgbb".equalsIgnoreCase(imageLocation);

    // Prepare attachments for processing
    List<Map.Entry<String, String>> newAttachments = new ArrayList<>();
    List<Map.Entry<String, String>> existingAttachments = new ArrayList<>();

    for (Map.Entry<String, String> attachment : attachments.entrySet()) {
      String fileName = attachment.getKey();
      String data = attachment.getValue();

      if (fileName == null || fileName.trim().isEmpty() || data == null || data.trim().isEmpty()) {
        throw new BadRequestException(
            ErrorMessages.PurchaseOrderErrorMessages.INVALID_ATTACHMENT_DATA);
      }

      // Separate new base64 uploads from existing URLs
      if (data.startsWith("http")) {
        // Existing URL - save directly to database
        existingAttachments.add(attachment);
      } else {
        // New base64 data - needs processing
        newAttachments.add(attachment);
      }
    }

    // Save existing URLs directly to database
    for (Map.Entry<String, String> attachment : existingAttachments) {
      Resources resource =
          new Resources(
              purchaseOrderId,
              EntityType.PURCHASE_ORDER,
              attachment.getKey(),
              attachment.getValue(),
              null);
      resourcesRepository.save(resource);
    }

    // Process new base64 attachments
    if (newAttachments.isEmpty()) {
      return; // No new attachments to process
    }

    if (isImgbbConfigured) {
      // Upload to ImgBB and save URLs
      Client client =
          clientRepository
              .findById(clientId)
              .orElseThrow(
                  () -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));
      String imgbbApiKey = client.getImgbbApiKey();

      if (imgbbApiKey == null || imgbbApiKey.trim().isEmpty()) {
        throw new BadRequestException(
            ErrorMessages.PurchaseOrderErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED);
      }

      // Get environment name for custom file naming
      String environmentName =
          environment.getActiveProfiles().length > 0
              ? environment.getActiveProfiles()[0]
              : "default";

      // Prepare attachment upload requests (keep track of fileName -> request mapping)
      List<ImgbbHelper.AttachmentUploadRequest> uploadRequests = new ArrayList<>();
      Map<Integer, String> indexToFileNameMap = new HashMap<>(); // Track index -> fileName mapping

      int index = 0;
      for (Map.Entry<String, String> attachment : newAttachments) {
        String fileName = attachment.getKey();
        String base64Data = attachment.getValue();

        // Store fileName for this index
        indexToFileNameMap.put(index, fileName);
        uploadRequests.add(new ImgbbHelper.AttachmentUploadRequest(fileName, base64Data, null));
        index++;
      }

      // Upload all attachments using ImgbbHelper
      ImgbbHelper imgbbHelper = new ImgbbHelper(imgbbApiKey);
      List<ImgbbHelper.AttachmentUploadResult> uploadResults;
      try {
        uploadResults =
            imgbbHelper.uploadPurchaseOrderAttachments(
                uploadRequests, environmentName, client.getName(), purchaseOrderId);
      } catch (IOException e) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.PurchaseOrderErrorMessages.FAILED_TO_UPLOAD_ATTACHMENTS,
                e.getMessage()));
      }

      // Save resource records to database with ImgBB URLs
      for (int i = 0; i < uploadResults.size(); i++) {
        ImgbbHelper.AttachmentUploadResult result = uploadResults.get(i);
        String fileName = indexToFileNameMap.get(i);

        Resources resource =
            new Resources(
                purchaseOrderId,
                EntityType.PURCHASE_ORDER,
                fileName,
                result.getUrl(),
                result.getDeleteHash(),
                result.getNotes());
        resourcesRepository.save(resource);
      }
    } else {
      // ImgBB not configured - save base64 data directly to database
      // Note: This stores base64 in the value field, which may be large
      // Consider implementing a file storage solution for production
      for (Map.Entry<String, String> attachment : newAttachments) {
        Resources resource =
            new Resources(
                purchaseOrderId,
                EntityType.PURCHASE_ORDER,
                attachment.getKey(),
                "data:image/png;base64," + attachment.getValue(),
                null);
        resourcesRepository.save(resource);
      }
    }
  }
}
