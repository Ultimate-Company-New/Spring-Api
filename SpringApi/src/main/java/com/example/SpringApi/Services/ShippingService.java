package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.FilterQueryBuilder.ShipmentFilterQueryBuilder;
import com.example.SpringApi.Helpers.PackagingHelper;
import com.example.SpringApi.Helpers.ShipRocketHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.*;
import com.example.SpringApi.Models.ResponseModels.*;
import com.example.SpringApi.Models.ShippingResponseModel.*;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.Interface.IPaymentSubTranslator;
import com.example.SpringApi.Services.Interface.IShippingSubTranslator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Helper class for shipping-related operations.
 * Consolidates functionality from ShipmentProcessingService, ShipmentService,
 * and ShippingService.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ShippingService extends BaseService implements IShippingSubTranslator {
    private static final String PRODUCT_ERROR_PREFIX = "Product '";
    private static final String UNKNOWN_LOCATION_NAME = "Unknown";

    /**
     * Maximum weight per shipment in kg.
     */
    private static final BigDecimal MAX_WEIGHT_PER_SHIPMENT = BigDecimal.valueOf(150);

    /**
     * Timeout for shipping API calls in seconds.
     */
    private static final int SHIPPING_API_TIMEOUT_SECONDS = 30;

    // Valid columns for shipment filtering
    private static final Set<String> VALID_SHIPMENT_COLUMNS = Set.of(
            "shipmentId", "orderSummaryId", "pickupLocationId", "totalWeightKgs", "totalQuantity",
            "expectedDeliveryDate", "packagingCost", "shippingCost", "totalCost",
            "selectedCourierCompanyId", "selectedCourierName", "selectedCourierRate", "selectedCourierMinWeight",
            "shipRocketOrderId", "shipRocketShipmentId", "shipRocketAwbCode", "shipRocketTrackingId",
            "shipRocketStatus",
            "createdUser", "modifiedUser", "createdAt", "updatedAt");

    private final ClientService clientService;
    private final ProductRepository productRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;
    private final PackagingHelper packagingHelper;
    private final ShipmentRepository shipmentRepository;
    private final ReturnShipmentRepository returnShipmentRepository;
    private final ReturnShipmentProductRepository returnShipmentProductRepository;
    private final ShipmentProductRepository shipmentProductRepository;
    private final ShipmentPackageRepository shipmentPackageRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderSummaryRepository orderSummaryRepository;
    private final IPaymentSubTranslator paymentService;
    private final PickupLocationRepository pickupLocationRepository;
    private final PackageRepository packageRepository;
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    private final ShipmentFilterQueryBuilder shipmentFilterQueryBuilder;

    @Autowired
    public ShippingService(
            ClientService clientService,
            ProductRepository productRepository,
            ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
            PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
            PackagingHelper packagingHelper,
            ShipmentRepository shipmentRepository,
            ReturnShipmentRepository returnShipmentRepository,
            ReturnShipmentProductRepository returnShipmentProductRepository,
            ShipmentProductRepository shipmentProductRepository,
            ShipmentPackageRepository shipmentPackageRepository,
            ShipmentPackageProductRepository shipmentPackageProductRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            OrderSummaryRepository orderSummaryRepository,
            IPaymentSubTranslator paymentService,
            PickupLocationRepository pickupLocationRepository,
            PackageRepository packageRepository,
            ClientRepository clientRepository,
            UserLogService userLogService,
            ShipmentFilterQueryBuilder shipmentFilterQueryBuilder,
            JwtTokenProvider jwtTokenProvider,
            HttpServletRequest request) {
        super(jwtTokenProvider, request);
        this.clientService = clientService;
        this.productRepository = productRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.packagingHelper = packagingHelper;
        this.shipmentRepository = shipmentRepository;
        this.returnShipmentRepository = returnShipmentRepository;
        this.returnShipmentProductRepository = returnShipmentProductRepository;
        this.shipmentProductRepository = shipmentProductRepository;
        this.shipmentPackageRepository = shipmentPackageRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderSummaryRepository = orderSummaryRepository;
        this.paymentService = paymentService;
        this.pickupLocationRepository = pickupLocationRepository;
        this.packageRepository = packageRepository;
        this.clientRepository = clientRepository;
        this.userLogService = userLogService;
        this.shipmentFilterQueryBuilder = shipmentFilterQueryBuilder;
    }

    // ============================================================================
    // PUBLIC API METHODS (called by Controller and other services)
    // ============================================================================

    /**
     * Retrieves shipments in batches with pagination support.
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationBaseResponseModel<ShipmentResponseModel> getShipmentsInBatches(
            PaginationBaseRequestModel paginationBaseRequestModel) {

        Long clientId = getClientId();

        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                if (filter.getColumn() != null && !VALID_SHIPMENT_COLUMNS.contains(filter.getColumn())) {
                    throw new BadRequestException(String
                            .format(ErrorMessages.ShipmentErrorMessages.INVALID_COLUMN_NAME_FORMAT, filter.getColumn()));
                }
                if (!filter.isValidOperator()) {
                    throw new BadRequestException(String
                            .format(ErrorMessages.ShipmentErrorMessages.INVALID_OPERATOR_FORMAT, filter.getOperator()));
                }
                String columnType = shipmentFilterQueryBuilder.getColumnType(filter.getColumn());
                filter.validateOperatorForType(columnType, filter.getColumn());
                filter.validateValuePresence();
            }
        }

        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        if (pageSize <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
        }

        Pageable pageable = new PageRequest(0, pageSize, Sort.by("createdAt").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        Page<Shipment> result = shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                clientId,
                paginationBaseRequestModel.getSelectedIds(),
                paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator()
                        : "AND",
                paginationBaseRequestModel.getFilters(),
                pageable);

        List<ShipmentResponseModel> data = result.getContent().stream()
                .map(shipment -> {
                    initializeShipmentLazyFields(shipment);
                    return new ShipmentResponseModel(shipment);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        return new PaginationBaseResponseModel<>(data, result.getTotalElements());
    }

    /**
     * Retrieves detailed information about a specific shipment by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ShipmentResponseModel getShipmentById(Long shipmentId) {
        if (shipmentId == null || shipmentId <= 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.INVALID_ID);
        }

        Long clientId = getClientId();

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, shipmentId)));

        if (!shipment.getClientId().equals(clientId)) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, shipmentId));
        }

        if (shipment.getShipRocketOrderId() == null || shipment.getShipRocketOrderId().trim().isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, shipmentId));
        }

        initializeShipmentLazyFields(shipment);
        return new ShipmentResponseModel(shipment);
    }

    /**
     * Initializes lazy-loaded entities on a shipment for response mapping.
     */
    private void initializeShipmentLazyFields(Shipment shipment) {
        Hibernate.initialize(shipment.getOrderSummary());
        if (shipment.getOrderSummary() != null) {
            Hibernate.initialize(shipment.getOrderSummary().getEntityAddress());
        }
        Hibernate.initialize(shipment.getPickupLocation());
        if (shipment.getPickupLocation() != null) {
            Hibernate.initialize(shipment.getPickupLocation().getAddress());
        }
        Hibernate.initialize(shipment.getShipmentProducts());
        if (shipment.getShipmentProducts() != null) {
            for (var sp : shipment.getShipmentProducts()) {
                Hibernate.initialize(sp.getProduct());
            }
        }
        Hibernate.initialize(shipment.getShipmentPackages());
        if (shipment.getShipmentPackages() != null) {
            for (var pkg : shipment.getShipmentPackages()) {
                Hibernate.initialize(pkg.getPackageInfo());
                Hibernate.initialize(pkg.getShipmentPackageProducts());
                if (pkg.getShipmentPackageProducts() != null) {
                    for (var spp : pkg.getShipmentPackageProducts()) {
                        Hibernate.initialize(spp.getProduct());
                    }
                }
            }
        }
        Hibernate.initialize(shipment.getReturnShipments());
        if (shipment.getReturnShipments() != null) {
            for (var rs : shipment.getReturnShipments()) {
                Hibernate.initialize(rs.getReturnProducts());
            }
        }
    }

    // ============================================================================
    // SHIPMENT PROCESSING SERVICE METHODS
    // ============================================================================

    /**
     * Processes shipments after cash payment approval.
     */
    @Transactional
    public PaymentVerificationResponseModel processShipmentsAfterPaymentApproval(
            Long purchaseOrderId,
            CashPaymentRequestModel cashPaymentRequest) {

        Long clientId = getClientId();
        Long userId = getUserId();
        String userName = getUser();

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID));

        if (!purchaseOrder.getClientId().equals(clientId)) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER);
        }

        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException(ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID);
        }

        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                        purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND));

        List<Shipment> shipments = shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
        if (shipments == null || shipments.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NO_SHIPMENTS_FOUND);
        }

        validateProductAndPackageAvailability(shipments);

        PaymentVerificationResponseModel paymentResponse = paymentService.recordCashPayment(cashPaymentRequest);

        if (!paymentResponse.isSuccess()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShipmentProcessingErrorMessages.OPERATION_FAILED_WITH_MESSAGE_FORMAT,
                            ErrorMessages.OPERATION_FAILED, paymentResponse.getMessage()));
        }

        updateInventory(shipments, userName);

        createShipRocketOrdersAndUpdateShipments(shipments, orderSummary, purchaseOrder, clientId, userName);

        userLogService.logData(
                userId,
                "Shipments processed successfully for PO #" + purchaseOrderId +
                        " (" + purchaseOrder.getVendorNumber() + "). Status: " + purchaseOrder.getPurchaseOrderStatus(),
                "ShipmentProcessing/processShipmentsAfterPaymentApproval");

        return paymentResponse;
    }

    /**
     * Processes shipments after online payment approval.
     */
    @Transactional
    public PaymentVerificationResponseModel processShipmentsAfterPaymentApproval(
            Long purchaseOrderId,
            RazorpayVerifyRequestModel razorpayVerifyRequest) {

        Long clientId = getClientId();
        Long userId = getUserId();
        String userName = getUser();

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID));

        if (!purchaseOrder.getClientId().equals(clientId)) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER);
        }

        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException(ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID);
        }

        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                        purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND));

        List<Shipment> shipments = shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
        if (shipments == null || shipments.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NO_SHIPMENTS_FOUND);
        }

        validateProductAndPackageAvailability(shipments);

        PaymentVerificationResponseModel paymentResponse = paymentService.verifyPayment(razorpayVerifyRequest);

        if (!paymentResponse.isSuccess()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShipmentProcessingErrorMessages.OPERATION_FAILED_WITH_MESSAGE_FORMAT,
                            ErrorMessages.OPERATION_FAILED, paymentResponse.getMessage()));
        }

        updateInventory(shipments, userName);

        createShipRocketOrdersAndUpdateShipments(shipments, orderSummary, purchaseOrder, clientId, userName);

        userLogService.logData(
                userId,
                "Shipments processed successfully for PO #" + purchaseOrderId +
                        " (" + purchaseOrder.getVendorNumber() + "). Status: " + purchaseOrder.getPurchaseOrderStatus(),
                "ShipmentProcessing/processShipmentsAfterPaymentApproval");

        return paymentResponse;
    }

    /**
     * Validates product and package availability at each pickup location.
     */
    private void validateProductAndPackageAvailability(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            Long pickupLocationId = shipment.getPickupLocationId();

            List<ShipmentProduct> shipmentProducts = shipmentProductRepository
                    .findByShipmentId(shipment.getShipmentId());
            for (ShipmentProduct shipmentProduct : shipmentProducts) {
                ProductPickupLocationMapping mapping = productPickupLocationMappingRepository
                        .findByProductIdAndPickupLocationId(
                                shipmentProduct.getProductId(),
                                pickupLocationId)
                        .orElseThrow(() -> new BadRequestException(String.format(
                                ErrorMessages.ShipmentProcessingErrorMessages.PRODUCT_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT,
                                shipmentProduct.getProductId(), pickupLocationId)));

                if (mapping.getAvailableStock() < shipmentProduct.getAllocatedQuantity()) {
                    throw new BadRequestException(String.format(
                            ErrorMessages.ShipmentProcessingErrorMessages.INSUFFICIENT_PRODUCT_STOCK_FORMAT,
                            shipmentProduct.getProductId(), pickupLocationId,
                            mapping.getAvailableStock(), shipmentProduct.getAllocatedQuantity()));
                }
            }

            List<ShipmentPackage> shipmentPackages = shipmentPackageRepository
                    .findByShipmentId(shipment.getShipmentId());
            for (ShipmentPackage shipmentPackage : shipmentPackages) {
                PackagePickupLocationMapping mapping = packagePickupLocationMappingRepository
                        .findByPackageIdAndPickupLocationId(
                                shipmentPackage.getPackageId(),
                                pickupLocationId)
                        .orElseThrow(() -> new BadRequestException(String.format(
                                ErrorMessages.ShipmentProcessingErrorMessages.PACKAGE_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT,
                                shipmentPackage.getPackageId(), pickupLocationId)));

                if (mapping.getAvailableQuantity() < shipmentPackage.getQuantityUsed()) {
                    throw new BadRequestException(String.format(
                            ErrorMessages.ShipmentProcessingErrorMessages.INSUFFICIENT_PACKAGE_STOCK_FORMAT,
                            shipmentPackage.getPackageId(), pickupLocationId,
                            mapping.getAvailableQuantity(), shipmentPackage.getQuantityUsed()));
                }
            }
        }
    }

    /**
     * Updates inventory by reducing product and package quantities.
     */
    private void updateInventory(List<Shipment> shipments, String userName) {
        for (Shipment shipment : shipments) {
            Long pickupLocationId = shipment.getPickupLocationId();

            List<ShipmentProduct> shipmentProducts = shipmentProductRepository
                    .findByShipmentId(shipment.getShipmentId());
            for (ShipmentProduct shipmentProduct : shipmentProducts) {
                ProductPickupLocationMapping mapping = productPickupLocationMappingRepository
                        .findByProductIdAndPickupLocationId(
                                shipmentProduct.getProductId(),
                                pickupLocationId)
                        .orElse(null);

                if (mapping != null) {
                    mapping.reduceStock(shipmentProduct.getAllocatedQuantity(), userName);
                    productPickupLocationMappingRepository.save(mapping);
                }
            }

            List<ShipmentPackage> shipmentPackages = shipmentPackageRepository
                    .findByShipmentId(shipment.getShipmentId());
            for (ShipmentPackage shipmentPackage : shipmentPackages) {
                PackagePickupLocationMapping mapping = packagePickupLocationMappingRepository
                        .findByPackageIdAndPickupLocationId(
                                shipmentPackage.getPackageId(),
                                pickupLocationId)
                        .orElse(null);

                if (mapping != null) {
                    mapping.reduceQuantity(shipmentPackage.getQuantityUsed(), userName);
                    packagePickupLocationMappingRepository.save(mapping);
                }
            }
        }
    }

    /**
     * Creates ShipRocket orders for each shipment and updates shipment records.
     */
    private void createShipRocketOrdersAndUpdateShipments(
            List<Shipment> shipments,
            OrderSummary orderSummary,
            PurchaseOrder purchaseOrder,
            Long clientId,
            String userName) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));

        if (client.getShipRocketEmail() == null || client.getShipRocketEmail().trim().isEmpty() ||
                client.getShipRocketPassword() == null || client.getShipRocketPassword().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED);
        }

        ShipRocketHelper shipRocketHelper = createShipRocketHelper(client.getShipRocketEmail(),
            client.getShipRocketPassword());

        Address deliveryAddress = orderSummary.getEntityAddress();
        if (deliveryAddress == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.DELIVERY_ADDRESS_NOT_FOUND);
        }

        Map<Long, PickupLocation> pickupLocationMap = new HashMap<>();
        for (Shipment shipment : shipments) {
            PickupLocation pickupLocation = pickupLocationRepository.findById(shipment.getPickupLocationId())
                    .orElseThrow(() -> new NotFoundException(String.format(
                            ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, shipment.getPickupLocationId())));
            pickupLocationMap.put(shipment.getPickupLocationId(), pickupLocation);
        }

        for (Shipment shipment : shipments) {
            PickupLocation pickupLocation = pickupLocationMap.get(shipment.getPickupLocationId());

            ShipRocketOrderRequestModel orderRequest = buildShipRocketOrderRequest(
                    shipment, orderSummary, purchaseOrder, deliveryAddress, pickupLocation, clientId);

            ShipRocketOrderResponseModel shipRocketResponse = shipRocketHelper.createCustomOrder(orderRequest);

            validateShipRocketOrderResponse(shipRocketResponse, shipment.getShipmentId());

            updateShipmentWithShipRocketResponse(shipment, shipRocketResponse, shipRocketHelper, userName);
        }
    }

    /**
     * Builds ShipRocket order request model.
     */
    private ShipRocketOrderRequestModel buildShipRocketOrderRequest(
            Shipment shipment,
            OrderSummary orderSummary,
            PurchaseOrder purchaseOrder,
            Address deliveryAddress,
            PickupLocation pickupLocation,
            Long clientId) {

        ShipRocketOrderRequestModel request = new ShipRocketOrderRequestModel();

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));

        request.setOrderId("PO_" + purchaseOrder.getPurchaseOrderId());
        request.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        if (pickupLocation.getAddressNickName() == null || pickupLocation.getAddressNickName().trim().isEmpty()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.PICKUP_LOCATION_NAME_NOT_CONFIGURED,
                            pickupLocation.getPickupLocationId()));
        }
        request.setPickupLocation(pickupLocation.getAddressNickName().trim());

        request.setChannelId("");

        if (client.getName() != null && !client.getName().trim().isEmpty()) {
            request.setCompanyName(client.getName().trim());
        }

        if (purchaseOrder.getVendorNumber() != null && !purchaseOrder.getVendorNumber().trim().isEmpty()) {
            String vendorInfo = "Vendor: " + purchaseOrder.getVendorNumber().trim();
            request.setComment(vendorInfo);
            request.setResellerName(vendorInfo);
        }

        String[] nameParts = splitName(deliveryAddress.getNameOnAddress());
        populateBillingAndShippingFromAddress(request, deliveryAddress, nameParts);

        List<ShipRocketOrderRequestModel.OrderItem> orderItems = new ArrayList<>();
        List<ShipmentProduct> shipmentProducts = shipmentProductRepository.findByShipmentId(shipment.getShipmentId());
        for (ShipmentProduct shipmentProduct : shipmentProducts) {
            Product product = productRepository.findById(shipmentProduct.getProductId()).orElse(null);
            if (product != null) {
                String name = product.getTitle() != null ? product.getTitle() : "";
                String sku = product.getUpc() != null && !product.getUpc().trim().isEmpty()
                        ? product.getUpc()
                        : "SKU-" + product.getProductId();
                int units = shipmentProduct.getAllocatedQuantity();
                int sellingPrice = (int) Math.round(shipmentProduct.getAllocatedPrice().doubleValue());
                Integer discount = null;
                if (product.getDiscount() != null && product.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = product.getIsDiscountPercent() != null && product.getIsDiscountPercent()
                            ? shipmentProduct.getAllocatedPrice().multiply(product.getDiscount())
                                    .divide(new BigDecimal("100"))
                            : product.getDiscount();
                    discount = (int) Math.round(discountAmount.doubleValue());
                }
                Integer tax = orderSummary.getGstPercentage() != null ? orderSummary.getGstPercentage().intValue()
                        : null;
                orderItems
                        .add(new ShipRocketOrderRequestModel.OrderItem(name, sku, units, sellingPrice, discount, tax));
            }
        }
        request.setOrderItems(orderItems);

        request.setPaymentMethod("prepaid");

        if (orderSummary.getTotalShipping() != null && orderSummary.getTotalShipping().compareTo(BigDecimal.ZERO) > 0) {
            request.setShippingCharges((int) Math.round(orderSummary.getTotalShipping().doubleValue()));
        }

        if (orderSummary.getTotalDiscount() != null && orderSummary.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0) {
            request.setTotalDiscount((int) Math.round(orderSummary.getTotalDiscount().doubleValue()));
        }

        request.setSubTotal((int) Math.round(orderSummary.getSubtotal().doubleValue()));

        int totalLength = 0;
        int totalBreadth = 0;
        int totalHeight = 0;

        List<ShipmentPackage> shipmentPackages = shipmentPackageRepository.findByShipmentId(shipment.getShipmentId());
        for (ShipmentPackage shipmentPackage : shipmentPackages) {
            com.example.SpringApi.Models.DatabaseModels.Package packageEntity = packageRepository
                    .findById(shipmentPackage.getPackageId())
                    .orElseThrow(() -> new NotFoundException(String.format(
                            ErrorMessages.PackageErrorMessages.INVALID_ID_WITH_ID_FORMAT, shipmentPackage.getPackageId())));

            totalLength += packageEntity.getLength() * shipmentPackage.getQuantityUsed();
            totalBreadth += packageEntity.getBreadth() * shipmentPackage.getQuantityUsed();
            totalHeight += packageEntity.getHeight() * shipmentPackage.getQuantityUsed();
        }

        request.setLength(totalLength > 0 ? (double) totalLength : 10.0);
        request.setBreadth(totalBreadth > 0 ? (double) totalBreadth : 10.0);
        request.setHeight(totalHeight > 0 ? (double) totalHeight : 10.0);
        request.setWeight(shipment.getTotalWeightKgs().doubleValue());

        request.setCodAmount(0.0);
        request.setCourierId(shipment.getSelectedCourierCompanyId());

        if (purchaseOrder.getPurchaseOrderReceipt() != null
                && !purchaseOrder.getPurchaseOrderReceipt().trim().isEmpty()) {
            request.setInvoiceNumber(purchaseOrder.getPurchaseOrderReceipt().trim());
        }

        request.setIsInsuranceOpt(false);
        request.setIsDocument(0);

        if (purchaseOrder.getVendorNumber() != null && !purchaseOrder.getVendorNumber().trim().isEmpty()) {
            request.setOrderTag(purchaseOrder.getVendorNumber().trim());
        }

        return request;
    }

    /**
     * Populates billing and shipping address fields on the request from the
     * delivery address.
     */
    private void populateBillingAndShippingFromAddress(ShipRocketOrderRequestModel request, Address deliveryAddress,
            String[] nameParts) {
        int pincode;
        try {
            pincode = Integer.parseInt(deliveryAddress.getPostalCode());
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.BILLING_POSTAL_CODE_MUST_BE_NUMERIC,
                            deliveryAddress.getPostalCode()));
        }
        String phone = cleanPhoneNumber(deliveryAddress.getPhoneOnAddress());
        if (phone.length() != 10) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.BILLING_PHONE_MUST_BE10_DIGITS,
                    deliveryAddress.getPhoneOnAddress() != null ? deliveryAddress.getPhoneOnAddress() : "empty"));
        }
        long phoneLong = Long.parseLong(phone);

        String street1 = deliveryAddress.getStreetAddress() != null ? deliveryAddress.getStreetAddress() : "";
        String street2 = deliveryAddress.getStreetAddress2() != null
                && !deliveryAddress.getStreetAddress2().trim().isEmpty()
                        ? deliveryAddress.getStreetAddress2()
                        : null;
        String city = deliveryAddress.getCity() != null ? deliveryAddress.getCity() : "";
        String state = deliveryAddress.getState() != null ? deliveryAddress.getState() : "";
        String country = deliveryAddress.getCountry() != null ? deliveryAddress.getCountry() : "";
        String email = deliveryAddress.getEmailOnAddress() != null ? deliveryAddress.getEmailOnAddress() : "";

        request.setBillingCustomerName(nameParts[0]);
        request.setBillingLastName(nameParts.length > 1 && !nameParts[1].isEmpty() ? nameParts[1] : null);
        request.setBillingAddress(street1);
        request.setBillingAddress2(street2);
        request.setBillingCity(city);
        request.setBillingPincode(pincode);
        request.setBillingState(state);
        request.setBillingCountry(country);
        request.setBillingEmail(email);
        request.setBillingPhone(phoneLong);
        request.setBillingIsdCode("+91");

        request.setShippingIsBilling(true);
        request.setShippingCustomerName(nameParts[0]);
        request.setShippingLastName(nameParts.length > 1 && !nameParts[1].isEmpty() ? nameParts[1] : null);
        request.setShippingAddress(street1);
        request.setShippingAddress2(street2);
        request.setShippingCity(city);
        request.setShippingPincode(pincode);
        request.setShippingState(state);
        request.setShippingCountry(country);
        request.setShippingEmail(
                deliveryAddress.getEmailOnAddress() != null && !deliveryAddress.getEmailOnAddress().trim().isEmpty()
                        ? deliveryAddress.getEmailOnAddress()
                        : null);
        request.setShippingPhone(phoneLong);
    }

    /**
     * Helper method to split a full name into first name and last name.
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[] { "", "" };
        }
        String trimmed = fullName.trim();
        int spaceIndex = trimmed.indexOf(" ");
        if (spaceIndex > 0 && spaceIndex < trimmed.length() - 1) {
            return new String[] {
                    trimmed.substring(0, spaceIndex),
                    trimmed.substring(spaceIndex + 1)
            };
        }
        return new String[] { trimmed, "" };
    }

    /**
     * Validates ShipRocket order creation response.
     */
    private void validateShipRocketOrderResponse(
            ShipRocketOrderResponseModel shipRocketResponse,
            Long shipmentId) {

        if (shipRocketResponse == null) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_API_NULL_RESPONSE, shipmentId));
        }

        if (shipRocketResponse.getMessage() != null && !shipRocketResponse.getMessage().trim().isEmpty()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED, shipmentId,
                            shipRocketResponse.getMessage()));
        }

        if (shipRocketResponse.getOrderId() == null) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED, shipmentId,
                    ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_ID_MISSING));
        }

        if (shipRocketResponse.getShipmentId() == null) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED, shipmentId,
                    ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_SHIPMENT_ID_MISSING));
        }

        if (shipRocketResponse.getStatus() == null || shipRocketResponse.getStatus().trim().isEmpty()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED, shipmentId,
                    ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_STATUS_MISSING));
        }

        if (!Shipment.ShipRocketStatus.isValid(shipRocketResponse.getStatus())) {
            String validStatuses = String.join(", ",
                    java.util.Arrays.stream(Shipment.ShipRocketStatus.values())
                            .map(Shipment.ShipRocketStatus::getValue)
                            .toArray(String[]::new));
            throw new BadRequestException(
                    String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED, shipmentId,
                            String.format(ErrorMessages.ShippingErrorMessages.INVALID_SHIP_ROCKET_STATUS_FORMAT,
                                    shipRocketResponse.getStatus(), validStatuses)));
        }
    }

    /**
     * Updates shipment with ShipRocket response data.
     */
    private void updateShipmentWithShipRocketResponse(
            Shipment shipment,
            ShipRocketOrderResponseModel shipRocketResponse,
            ShipRocketHelper shipRocketHelper,
            String userName) {

        shipment.populateFromShipRocketOrderResponse(shipRocketResponse);
        String shipRocketOrderId = shipRocketResponse.getOrderIdAsString();

        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipmentId();
            Long courierId = shipment.getSelectedCourierCompanyId();

            if (shipRocketShipmentId != null && courierId != null) {
                String awbMetadataJson = shipRocketHelper.assignAwbAsJson(shipRocketShipmentId, courierId);
                shipment.setShipRocketAwbMetadata(awbMetadataJson);

                Gson gson = new Gson();
                com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel awbResponse = gson
                        .fromJson(awbMetadataJson,
                                com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel.class);

                if (awbResponse != null && awbResponse.isSuccess() && awbResponse.getAwbCode() != null) {
                    shipment.setShipRocketAwbCode(awbResponse.getAwbCode());
                } else {
                    shipment.setShipRocketAwbCode(shipRocketResponse.getAwbCode());
                }
            } else {
                shipment.setShipRocketAwbCode(shipRocketResponse.getAwbCode());
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.AWB_ASSIGNMENT_FAILED,
                    shipRocketResponse.getShipmentId(), e.getMessage()));
        }

        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipmentId();
            if (shipRocketShipmentId != null) {
                String pickupMetadataJson = shipRocketHelper.generatePickupAsJson(shipRocketShipmentId);
                shipment.setShipRocketPickupMetadata(pickupMetadataJson);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.PICKUP_GENERATION_FAILED,
                    shipRocketResponse.getShipmentId(), e.getMessage()));
        }

        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipmentId();
            if (shipRocketShipmentId != null) {
                String manifestUrl = shipRocketHelper.generateManifest(shipRocketShipmentId);
                shipment.setShipRocketGeneratedManifestUrl(manifestUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.MANIFEST_GENERATION_FAILED,
                    shipRocketResponse.getShipmentId(), e.getMessage()));
        }

        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipmentId();
            if (shipRocketShipmentId != null) {
                String labelUrl = shipRocketHelper.generateLabel(shipRocketShipmentId);
                shipment.setShipRocketGeneratedLabelUrl(labelUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.LABEL_GENERATION_FAILED,
                    shipRocketResponse.getShipmentId(), e.getMessage()));
        }

        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipmentId();
            if (shipRocketShipmentId != null) {
                String invoiceUrl = shipRocketHelper.generateInvoice(shipRocketShipmentId);
                shipment.setShipRocketGeneratedInvoiceUrl(invoiceUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.INVOICE_GENERATION_FAILED,
                    shipRocketResponse.getShipmentId(), e.getMessage()));
        }

        try {
            String awbCode = shipment.getShipRocketAwbCode();
            if (awbCode != null && !awbCode.trim().isEmpty()) {
                String trackingJson = shipRocketHelper.getTrackingAsJson(awbCode);
                shipment.setShipRocketTrackingMetadata(trackingJson);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.TRACKING_FETCH_FAILED,
                    shipment.getShipRocketAwbCode(), e.getMessage()));
        }

        try {
            String orderDetailsJson = shipRocketHelper.getOrderDetailsAsJson(shipRocketOrderId);
            shipment.setShipRocketFullResponse(orderDetailsJson);
        } catch (Exception e) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String fullResponseJson = objectMapper.writeValueAsString(shipRocketResponse);
                shipment.setShipRocketFullResponse(fullResponseJson);
            } catch (Exception serializationEx) {
                BadRequestException exception = new BadRequestException(String.format(
                        ErrorMessages.ShippingErrorMessages.FAILED_TO_SERIALIZE_SHIP_ROCKET_RESPONSE_FORMAT,
                        shipment.getShipmentId(), serializationEx.getMessage()));
                exception.initCause(serializationEx);
                throw exception;
            }
        }

        shipment.setModifiedUser(userName);
        shipmentRepository.save(shipment);
    }

    /**
     * Cleans phone number by removing formatting and ensuring it's exactly 10
     * digits.
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String cleaned = phone.replaceAll("\\D", "");
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }
        return cleaned;
    }

    // ============================================================================
    // SHIPPING SERVICE METHODS
    // ============================================================================

    /**
     * Calculates available shipping options (couriers and rates) for an order.
     */
    @Override
    public ShippingCalculationResponseModel calculateShipping(ShippingCalculationRequestModel request) {
        if (request == null) {
            throw new NullPointerException(ErrorMessages.ShippingErrorMessages.NULL_SHIPPING_CALCULATION_REQUEST);
        }

        Long clientId = getClientId();
        ShippingCalculationResponseModel response = new ShippingCalculationResponseModel();
        response.setLocationOptions(new ArrayList<>());

        if (request.getPickupLocations() == null || request.getPickupLocations().isEmpty()) {
            return response;
        }

        ShipRocketHelper shipRocketHelper = getShiprocketHelper(clientId);
        String deliveryPostcode = request.getDeliveryPostcode();
        boolean isCod = Boolean.TRUE.equals(request.getIsCod());

        BigDecimal totalShippingCost = BigDecimal.ZERO;

        for (ShippingCalculationRequestModel.PickupLocationShipment location : request.getPickupLocations()) {
            ShippingCalculationResponseModel.LocationShippingOptions locationOptions = new ShippingCalculationResponseModel.LocationShippingOptions(
                    location.getPickupLocationId(),
                    location.getLocationName(),
                    location.getPickupPostcode(),
                    location.getTotalWeightKgs(),
                    location.getTotalQuantity(),
                    location.getProductIds());

            BigDecimal weight = location.getTotalWeightKgs();
            if (weight == null || weight.compareTo(BigDecimal.valueOf(0.5)) < 0) {
                weight = BigDecimal.valueOf(0.5);
            }

            try {
                ShippingOptionsResponseModel shippingOptions = shipRocketHelper.getAvailableShippingOptions(
                        location.getPickupPostcode(),
                        deliveryPostcode,
                        isCod,
                        weight.toString());

                if (shippingOptions != null && shippingOptions.getData() != null
                    && shippingOptions.getData().getAvailableCourierCompanies() != null) {

                    shippingOptions.getData().getAvailableCourierCompanies().sort(
                        (a, b) -> Double.compare(a.getRate(), b.getRate()));

                    for (var courier : shippingOptions.getData().getAvailableCourierCompanies()) {
                        locationOptions.getAvailableCouriers().add(
                                ShippingCalculationResponseModel.CourierOption.fromShiprocketCourier(courier));
                    }

                    if (!locationOptions.getAvailableCouriers().isEmpty()) {
                        locationOptions.setSelectedCourier(locationOptions.getAvailableCouriers().get(0));
                        totalShippingCost = totalShippingCost.add(
                                locationOptions.getAvailableCouriers().get(0).getRate());
                    }
                }
            } catch (Exception e) {
                // Continue with other locations on error
            }

            response.getLocationOptions().add(locationOptions);
        }

        response.setTotalShippingCost(totalShippingCost);
        return response;
    }

    /**
     * Creates a ShiprocketHelper instance with the current client's ShipRocket
     * credentials.
     */
    protected ShipRocketHelper getShiprocketHelper(Long clientId) {
        ClientResponseModel client = clientService.getClientById(clientId);
        return createShipRocketHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
    }

    /**
     * Factory method for ShipRocketHelper to allow test overrides.
     */
    protected ShipRocketHelper createShipRocketHelper(String email, String password) {
        return new ShipRocketHelper(email, password);
    }

    /**
     * Optimizes order fulfillment by finding the cheapest allocation of products
     * across pickup locations.
     */
    @Override
    public OrderOptimizationResponseModel optimizeOrder(OrderOptimizationRequestModel request) {
        Long clientId = getClientId();
        OrderOptimizationResponseModel response = new OrderOptimizationResponseModel();

        if (request.getProductQuantities() == null || request.getProductQuantities().isEmpty()) {
            return OrderOptimizationResponseModel
                    .error(ErrorMessages.OrderOptimizationErrorMessages.NO_PRODUCTS_SPECIFIED);
        }

        if (request.getDeliveryPostcode() == null || request.getDeliveryPostcode().isEmpty()) {
            return OrderOptimizationResponseModel
                    .error(ErrorMessages.OrderOptimizationErrorMessages.DELIVERY_POSTCODE_REQUIRED);
        }

        try {
            Map<Long, ProductLocationInfo> productInfoMap = fetchProductData(request.getProductQuantities());

            if (productInfoMap.isEmpty()) {
                return OrderOptimizationResponseModel
                        .error(ErrorMessages.OrderOptimizationErrorMessages.NO_VALID_PRODUCTS_FOUND);
            }

            Map<Long, LocationInfo> locationInfoMap = fetchLocationData(productInfoMap);

            List<AllocationCandidate> candidates;

            if (request.getCustomAllocations() != null && !request.getCustomAllocations().isEmpty()) {
                CustomAllocationResult customResult = createCustomAllocationCandidate(
                        request.getCustomAllocations(),
                        productInfoMap,
                        locationInfoMap);

                if (!customResult.isValid) {
                    return OrderOptimizationResponseModel.error(customResult.errorMessage);
                }

                candidates = new ArrayList<>();
                candidates.add(customResult.candidate);
            } else {
                String feasibilityError = checkFeasibility(productInfoMap, request.getProductQuantities(),
                        locationInfoMap);
                if (feasibilityError != null) {
                    return OrderOptimizationResponseModel.error(feasibilityError);
                }

                candidates = generateCandidates(
                        productInfoMap,
                        locationInfoMap,
                        request.getProductQuantities());

                if (candidates.isEmpty()) {
                    return OrderOptimizationResponseModel
                            .error(ErrorMessages.OrderOptimizationErrorMessages.NO_VALID_ALLOCATION_STRATEGIES_FOUND);
                }
            }

            ShipRocketHelper shipRocketHelper = getShiprocketHelper(clientId);
            String deliveryPostcode = request.getDeliveryPostcode();
            boolean isCod = Boolean.TRUE.equals(request.getIsCod());
            boolean isCustomAllocation = request.getCustomAllocations() != null
                    && !request.getCustomAllocations().isEmpty();

            evaluateCandidates(candidates, productInfoMap, locationInfoMap,
                    shipRocketHelper, deliveryPostcode, isCod, isCustomAllocation);

            List<AllocationCandidate> validCandidates = candidates.stream()
                    .filter(c -> c.allCouriersAvailable)
                    .sorted(Comparator.comparing(c -> c.totalCost))
                    .toList();

            List<AllocationCandidate> invalidCandidates = candidates.stream()
                    .filter(c -> !c.allCouriersAvailable)
                    .sorted(Comparator.comparing(c -> c.totalCost))
                    .toList();

            AllocationCandidate candidateToUse;
            if (!validCandidates.isEmpty()) {
                candidateToUse = validCandidates.getFirst();
            } else if (!invalidCandidates.isEmpty()) {
                candidateToUse = invalidCandidates.getFirst();
            } else {
                candidateToUse = null;
            }
            if (candidateToUse != null) {
                boolean allCouriersAvailable = !validCandidates.isEmpty();
                populateResponseFromCandidate(response, candidateToUse, allCouriersAvailable);
                if (!allCouriersAvailable) {
                    response.setUnavailabilityReason(candidateToUse.unavailabilityReason);
                    response.setErrorMessage(
                            ErrorMessages.OrderOptimizationErrorMessages.NO_SHIPPING_OPTIONS_FOR_ANY_STRATEGY);
                }
            }

            response.setTotalProductCount(request.getProductQuantities().size());
            response.setTotalQuantity(request.getProductQuantities().values().stream()
                    .mapToInt(Integer::intValue).sum());
            response.setSuccess(true);

        } catch (Exception e) {
            return OrderOptimizationResponseModel.error(String
                    .format(ErrorMessages.OrderOptimizationErrorMessages.OPTIMIZATION_FAILED_FORMAT, e.getMessage()));
        }

        return response;
    }

    // Helper classes for order optimization
    private static class ProductLocationInfo {
        Product productEntity;
        String productTitle;
        BigDecimal weightKgs;
        BigDecimal length;
        BigDecimal breadth;
        BigDecimal height;
        Map<Long, LocationStock> stockByLocation = new HashMap<>();
    }

    private static class LocationStock {
        int availableStock;
        int maxItemsPackable;
        List<PackagingHelper.PackageDimension> packageDimensions;
        String packagingErrorMessage;
    }

    private static class LocationInfo {
        PickupLocation pickupLocationEntity;
        String locationName;
        String postalCode;
        List<PackagingHelper.PackageDimension> packageDimensions = new ArrayList<>();
        Map<Long, com.example.SpringApi.Models.DatabaseModels.Package> packageEntities = new HashMap<>();
    }

    private static class ProductAllocationTracker {
        ProductResponseModel productResponseModel;
        int remainingQty;
        BigDecimal weightPerUnit;
    }

    private static class AllocationCandidate {
        Map<Long, Map<Long, Integer>> locationProductQuantities = new LinkedHashMap<>();
        BigDecimal totalPackagingCost = BigDecimal.ZERO;
        BigDecimal totalShippingCost = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<OrderOptimizationResponseModel.Shipment> shipments = new ArrayList<>();
        boolean canFulfillOrder = true;
        int shortfall = 0;
        boolean allCouriersAvailable = true;
        String unavailabilityReason = null;
    }

    private static class CustomAllocationResult {
        AllocationCandidate candidate;
        String errorMessage;
        boolean isValid;

        static CustomAllocationResult success(AllocationCandidate candidate) {
            CustomAllocationResult result = new CustomAllocationResult();
            result.candidate = candidate;
            result.isValid = true;
            return result;
        }

        static CustomAllocationResult error(String message) {
            CustomAllocationResult result = new CustomAllocationResult();
            result.errorMessage = message;
            result.isValid = false;
            return result;
        }
    }

    // Continuing with order optimization methods from ShippingService...
    // Due to character limits, I'll continue in the next file creation

    /**
     * Cancels an outbound shipment by calling ShipRocket cancel API.
     */
    @Override
    @Transactional
    public void cancelShipment(Long shipmentId) {
        Long clientId = getClientId();
        String userName = getUser();

        Shipment shipment = shipmentRepository.findByShipmentIdAndClientId(shipmentId, clientId);
        if (shipment == null) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, shipmentId));
        }

        if ("CANCELLED".equals(shipment.getShipRocketStatus())) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.ALREADY_CANCELLED);
        }

        String shipRocketOrderId = shipment.getShipRocketOrderId();
        if (shipRocketOrderId == null || shipRocketOrderId.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NO_SHIP_ROCKET_ORDER_ID);
        }

        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED);
        }

        ShipRocketHelper shiprocketHelper = createShipRocketHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword());

        try {
            Long shipRocketOrderIdLong = Long.parseLong(shipRocketOrderId);
            shiprocketHelper.cancelOrders(java.util.List.of(shipRocketOrderIdLong));
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShipmentErrorMessages.INVALID_ID_FORMAT_ERROR_FORMAT, shipRocketOrderId));
        } catch (Exception e) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ShipmentErrorMessages.INVALID_ID_WITH_MESSAGE_FORMAT, e.getMessage()));
        }

        shipment.setShipRocketStatus("CANCELLED");
        shipment.setUpdatedAt(java.time.LocalDateTime.now());
        shipment.setModifiedUser(userName);
        shipmentRepository.save(shipment);
    }

    /**
     * Creates a return order for a shipment (full or partial).
     */
    @Override
    @Transactional
    public ReturnShipmentResponseModel createReturn(CreateReturnRequestModel request) {
        Long clientId = getClientId();
        String currentUser = getUser();

        if (request.getShipmentId() == null) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.SHIPMENT_ID_REQUIRED);
        }
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.AT_LEAST_ONE_PRODUCT_REQUIRED);
        }

        Shipment shipment = shipmentRepository.findByShipmentIdAndClientId(request.getShipmentId(), clientId);
        if (shipment == null) {
            throw new NotFoundException(
                    String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, request.getShipmentId()));
        }

        if (!"DELIVERED".equals(shipment.getShipRocketStatus())) {
            throw new BadRequestException(String.format(
                    ErrorMessages.ReturnShipmentErrorMessages.ONLY_DELIVERED_CAN_RETURN, shipment.getShipRocketStatus()));
        }

        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED);
        }

        if (shipment.getShipmentProducts() == null || shipment.getShipmentProducts().isEmpty()) {
            Long requestedProductId = null;
            if (request.getProducts() != null && !request.getProducts().isEmpty()) {
                requestedProductId = request.getProducts().get(0).getProductId();
            }
            throw new BadRequestException(String.format(
                    ErrorMessages.ReturnShipmentErrorMessages.PRODUCT_NOT_IN_SHIPMENT, requestedProductId));
        }

        org.hibernate.Hibernate.initialize(shipment.getShipmentProducts());
        Map<Long, Integer> shipmentProductQuantities = new HashMap<>();
        for (var sp : shipment.getShipmentProducts()) {
            shipmentProductQuantities.put(sp.getProductId(), sp.getAllocatedQuantity());
        }

        List<Product> productsToReturn = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        int totalReturnableQuantity = 0;

        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            if (item.getProductId() == null) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.PRODUCT_ID_REQUIRED);
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.VALID_QUANTITY_REQUIRED);
            }
            if (item.getReason() == null || item.getReason().isEmpty()) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.RETURN_REASON_REQUIRED);
            }

            Integer shipmentQty = shipmentProductQuantities.get(item.getProductId());
            if (shipmentQty == null) {
                throw new BadRequestException(String
                        .format(ErrorMessages.ReturnShipmentErrorMessages.PRODUCT_NOT_IN_SHIPMENT, item.getProductId()));
            }

            if (item.getQuantity() > shipmentQty) {
                throw new BadRequestException(
                        String.format(ErrorMessages.ReturnShipmentErrorMessages.RETURN_QUANTITY_EXCEEDS,
                                item.getQuantity(), shipmentQty, item.getProductId()));
            }

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format(ErrorMessages.ProductErrorMessages.ER013, item.getProductId())));

            if (product.getReturnWindowDays() != null && product.getReturnWindowDays() > 0
                    && shipment.getDeliveredDate() != null) {
                java.time.LocalDateTime returnDeadline = shipment.getDeliveredDate()
                        .plusDays(product.getReturnWindowDays());
                if (java.time.LocalDateTime.now().isAfter(returnDeadline)) {
                    throw new BadRequestException(
                            String.format(ErrorMessages.ReturnShipmentErrorMessages.PRODUCT_PAST_RETURN_WINDOW,
                                    product.getTitle(), product.getReturnWindowDays()));
                }
            } else if (product.getReturnWindowDays() == null || product.getReturnWindowDays() == 0) {
                throw new BadRequestException(String
                        .format(ErrorMessages.ReturnShipmentErrorMessages.PRODUCT_NOT_RETURNABLE, product.getTitle()));
            }

            productsToReturn.add(product);

            BigDecimal sellingPrice = product.getPrice().subtract(product.getDiscount());
            subTotal = subTotal.add(sellingPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQuantity += item.getQuantity();
            totalReturnableQuantity += shipmentQty;
        }

        boolean isFullReturn = (totalQuantity >= totalReturnableQuantity);
        ReturnShipment.ReturnType returnType = isFullReturn ? ReturnShipment.ReturnType.FULL_RETURN
                : ReturnShipment.ReturnType.PARTIAL_RETURN;

        org.hibernate.Hibernate.initialize(shipment.getOrderSummary());
        org.hibernate.Hibernate.initialize(shipment.getOrderSummary().getEntityAddress());
        Address customerAddress = shipment.getOrderSummary().getEntityAddress();

        org.hibernate.Hibernate.initialize(shipment.getPickupLocation());
        org.hibernate.Hibernate.initialize(shipment.getPickupLocation().getAddress());
        PickupLocation warehouseLocation = shipment.getPickupLocation();
        Address warehouseAddress = warehouseLocation.getAddress();

        ShipRocketReturnOrderRequestModel shipRocketRequest = new ShipRocketReturnOrderRequestModel();

        shipRocketRequest.setOrderId("RET-" + shipment.getShipmentId() + "-" + System.currentTimeMillis());
        shipRocketRequest.setOrderDate(java.time.LocalDate.now().toString());

        shipRocketRequest.setPickupCustomerName(
                customerAddress.getNameOnAddress() != null ? customerAddress.getNameOnAddress() : "Customer");
        shipRocketRequest.setPickupLastName("");
        shipRocketRequest.setCompanyName(clientResponse.getName() != null ? clientResponse.getName() : "");
        shipRocketRequest.setPickupAddress(customerAddress.getStreetAddress());
        shipRocketRequest.setPickupAddress2(
                customerAddress.getStreetAddress2() != null ? customerAddress.getStreetAddress2() : "");
        shipRocketRequest.setPickupCity(customerAddress.getCity());
        shipRocketRequest.setPickupState(customerAddress.getState());
        shipRocketRequest
                .setPickupCountry(customerAddress.getCountry() != null ? customerAddress.getCountry() : "India");
        shipRocketRequest.setPickupPincode(customerAddress.getPostalCode());
        shipRocketRequest
                .setPickupEmail(customerAddress.getEmailOnAddress() != null ? customerAddress.getEmailOnAddress() : "");
        shipRocketRequest
                .setPickupPhone(customerAddress.getPhoneOnAddress() != null ? customerAddress.getPhoneOnAddress() : "");
        shipRocketRequest.setPickupIsdCode("91");

        shipRocketRequest.setShippingCustomerName(
                warehouseAddress.getNameOnAddress() != null ? warehouseAddress.getNameOnAddress()
                        : warehouseLocation.getAddressNickName());
        shipRocketRequest.setShippingLastName("");
        shipRocketRequest.setShippingAddress(warehouseAddress.getStreetAddress());
        shipRocketRequest.setShippingAddress2(
                warehouseAddress.getStreetAddress2() != null ? warehouseAddress.getStreetAddress2() : "");
        shipRocketRequest.setShippingCity(warehouseAddress.getCity());
        shipRocketRequest.setShippingState(warehouseAddress.getState());
        shipRocketRequest
                .setShippingCountry(warehouseAddress.getCountry() != null ? warehouseAddress.getCountry() : "India");
        shipRocketRequest.setShippingPincode(warehouseAddress.getPostalCode());
        shipRocketRequest.setShippingEmail(
                warehouseAddress.getEmailOnAddress() != null ? warehouseAddress.getEmailOnAddress() : "");
        shipRocketRequest.setShippingPhone(
                warehouseAddress.getPhoneOnAddress() != null ? warehouseAddress.getPhoneOnAddress() : "");
        shipRocketRequest.setShippingIsdCode("91");

        List<ShipRocketReturnOrderRequestModel.ReturnOrderItem> orderItems = new ArrayList<>();
        int productIndex = 0;
        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            Product product = productsToReturn.get(productIndex++);

            ShipRocketReturnOrderRequestModel.ReturnOrderItem orderItem = new ShipRocketReturnOrderRequestModel.ReturnOrderItem();
            orderItem.setName(product.getTitle());
            orderItem.setQcEnable(true);
            orderItem.setQcProductName(product.getTitle());
            orderItem.setSku(product.getUpc() != null ? product.getUpc() : "SKU-" + product.getProductId());
            orderItem.setUnits(item.getQuantity());
            orderItem.setSellingPrice(product.getPrice().subtract(product.getDiscount()));
            orderItem.setDiscount(BigDecimal.ZERO);
            orderItem.setQcBrand(product.getBrand());
            orderItem.setQcProductImage(product.getMainImageUrl());

            orderItems.add(orderItem);
        }
        shipRocketRequest.setOrderItems(orderItems);

        shipRocketRequest.setPaymentMethod("PREPAID");
        shipRocketRequest.setTotalDiscount("0");
        shipRocketRequest.setSubTotal(subTotal);

        shipRocketRequest.setLength(request.getLength() != null ? request.getLength() : BigDecimal.valueOf(11));
        shipRocketRequest.setBreadth(request.getBreadth() != null ? request.getBreadth() : BigDecimal.valueOf(11));
        shipRocketRequest.setHeight(request.getHeight() != null ? request.getHeight() : BigDecimal.valueOf(11));
        shipRocketRequest.setWeight(request.getWeight() != null ? request.getWeight() : BigDecimal.valueOf(0.5));

        ShipRocketHelper shipRocketHelper = createShipRocketHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword());

        String returnOrderJson;
        ShipRocketReturnOrderResponseModel returnOrderResponse;
        try {
            returnOrderJson = shipRocketHelper.createReturnOrderAsJson(shipRocketRequest);
            returnOrderResponse = new Gson().fromJson(returnOrderJson, ShipRocketReturnOrderResponseModel.class);
        } catch (Exception e) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ReturnShipmentErrorMessages.FAILED_TO_CREATE_RETURN, e.getMessage()));
        }

        ReturnShipment returnShipment = ReturnShipment.fromCreateReturn(
                shipment, returnType, returnOrderResponse, request, returnOrderJson, clientId, currentUser);

        returnShipment = returnShipmentRepository.save(returnShipment);

        productIndex = 0;
        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            Product product = productsToReturn.get(productIndex++);
            ReturnShipmentProduct returnProduct = ReturnShipmentProduct.fromReturnItem(
                    returnShipment.getReturnShipmentId(), item, product, clientId, currentUser);
            returnShipmentProductRepository.save(returnProduct);
        }

        try {
            String awbJson = shipRocketHelper.assignReturnAwbAsJson(returnOrderResponse.getShipmentId());
            ShipRocketAwbResponseModel awbResponse = new Gson().fromJson(awbJson, ShipRocketAwbResponseModel.class);

            returnShipment.setShipRocketReturnAwbCode(awbResponse.getAwbCode());
            returnShipment.setShipRocketReturnAwbMetadata(awbJson);
            returnShipmentRepository.save(returnShipment);
        } catch (Exception e) {
            // AWB can be assigned later
        }

        String newStatus = isFullReturn ? "FULL_RETURN_INITIATED" : "PARTIAL_RETURN_INITIATED";
        shipment.setShipRocketStatus(newStatus);
        shipment.setModifiedUser(currentUser);
        shipmentRepository.save(shipment);

        returnShipment = returnShipmentRepository.findByReturnShipmentIdAndClientId(
                returnShipment.getReturnShipmentId(), clientId);
        org.hibernate.Hibernate.initialize(returnShipment.getReturnProducts());

        return new ReturnShipmentResponseModel(returnShipment);
    }

    /**
     * Cancels a return shipment by calling ShipRocket cancel API.
     */
    @Override
    @Transactional
    public void cancelReturnShipment(Long returnShipmentId) {
        Long clientId = getClientId();
        String userName = getUser();

        ReturnShipment returnShipment = returnShipmentRepository.findByReturnShipmentIdAndClientId(returnShipmentId,
                clientId);
        if (returnShipment == null) {
            throw new NotFoundException(
                    String.format(ErrorMessages.ReturnShipmentErrorMessages.NOT_FOUND, returnShipmentId));
        }

        if (ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue()
                .equals(returnShipment.getShipRocketReturnStatus())) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.ALREADY_CANCELLED);
        }

        String shipRocketReturnOrderId = returnShipment.getShipRocketReturnOrderId();
        if (shipRocketReturnOrderId == null || shipRocketReturnOrderId.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.NO_SHIP_ROCKET_ORDER_ID);
        }

        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED);
        }

        ShipRocketHelper shiprocketHelper = createShipRocketHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword());

        try {
            Long shipRocketReturnOrderIdLong = Long.parseLong(shipRocketReturnOrderId);
            shiprocketHelper.cancelOrders(java.util.List.of(shipRocketReturnOrderIdLong));
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    ErrorMessages.ReturnShipmentErrorMessages.INVALID_ID + " Format error: " + shipRocketReturnOrderId);
        } catch (Exception e) {
            throw new BadRequestException(
                    String.format(ErrorMessages.ReturnShipmentErrorMessages.FAILED_TO_CANCEL_RETURN, e.getMessage()));
        }

        returnShipment.setShipRocketReturnStatus(ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue());
        returnShipment.setUpdatedAt(java.time.LocalDateTime.now());
        returnShipment.setModifiedUser(userName);
        returnShipmentRepository.save(returnShipment);
    }

    /**
     * Retrieves the client's ShipRocket wallet balance for shipping prepaid orders.
     */
    @Override
    public Double getWalletBalance() {
        Long clientId = getClientId();
        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED);
        }

        ShipRocketHelper shiprocketHelper = createShipRocketHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword());

        return shiprocketHelper.getWalletBalance();
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    /**
     * Fetches product entities and builds ProductLocationInfo map with dimensions
     * and weight for each product.
     */
    private Map<Long, ProductLocationInfo> fetchProductData(Map<Long, Integer> productQuantities) {
        Map<Long, ProductLocationInfo> result = new HashMap<>();

        List<Product> products = productRepository.findAllById(productQuantities.keySet());

        for (Product product : products) {
            ProductLocationInfo info = new ProductLocationInfo();
            info.productEntity = product;
            info.productTitle = product.getTitle();
            info.weightKgs = product.getWeightKgs() != null ? product.getWeightKgs() : BigDecimal.valueOf(0.5);
            info.length = product.getLength();
            info.breadth = product.getBreadth();
            info.height = product.getHeight();

            result.put(product.getProductId(), info);
        }

        return result;
    }

    /**
     * Fetches stock mappings, package availability per location, and computes max
     * packable items per product.
     */
    private Map<Long, LocationInfo> fetchLocationData(Map<Long, ProductLocationInfo> productInfoMap) {
        Map<Long, LocationInfo> locationInfoMap = new HashMap<>();

        for (Map.Entry<Long, ProductLocationInfo> productEntry : productInfoMap.entrySet()) {
            Long productId = productEntry.getKey();
            List<ProductPickupLocationMapping> mappings = productPickupLocationMappingRepository
                    .findByProductIdWithPickupLocationAndAddress(productId);

            ProductLocationInfo productInfo = productEntry.getValue();

            for (ProductPickupLocationMapping mapping : mappings) {
                Long locationId = mapping.getPickupLocationId();

                if (!locationInfoMap.containsKey(locationId)) {
                    LocationInfo locInfo = new LocationInfo();
                    locInfo.pickupLocationEntity = mapping.getPickupLocation();
                    locInfo.locationName = mapping.getPickupLocation() != null
                            ? mapping.getPickupLocation().getAddressNickName()
                            : "Location " + locationId;
                    locInfo.postalCode = mapping.getPickupLocation() != null &&
                            mapping.getPickupLocation().getAddress() != null
                                    ? mapping.getPickupLocation().getAddress().getPostalCode()
                                    : null;
                    locationInfoMap.put(locationId, locInfo);
                }

                LocationStock stock = new LocationStock();
                stock.availableStock = mapping.getAvailableStock() != null ? mapping.getAvailableStock() : 0;
                productInfo.stockByLocation.put(locationId, stock);
            }
        }

        if (!locationInfoMap.isEmpty()) {
            List<Long> locationIds = new ArrayList<>(locationInfoMap.keySet());

            List<PackagePickupLocationMapping> packageMappings = packagePickupLocationMappingRepository
                    .findByPickupLocationIdsWithPackages(locationIds);

            for (PackagePickupLocationMapping pm : packageMappings) {
                LocationInfo locInfo = locationInfoMap.get(pm.getPickupLocationId());
                if (locInfo != null && pm.getPackageEntity() != null) {
                    var pkg = pm.getPackageEntity();
                    locInfo.packageEntities.put(pkg.getPackageId(), pkg);
                    locInfo.packageDimensions.add(new PackagingHelper.PackageDimension(
                            pkg.getPackageId(), pkg.getPackageName(), pkg.getPackageType(),
                            new PackagingHelper.PackageDimension.PackageSize(
                                    pkg.getLength(),
                                    pkg.getBreadth(),
                                    pkg.getHeight()),
                            pkg.getMaxWeight(), pkg.getPricePerUnit(), pm.getAvailableQuantity()));
                }
            }
        }

        for (ProductLocationInfo productInfo : productInfoMap.values()) {
            for (Map.Entry<Long, LocationStock> entry : productInfo.stockByLocation.entrySet()) {
                Long locationId = entry.getKey();
                LocationStock stock = entry.getValue();
                LocationInfo locInfo = locationInfoMap.get(locationId);

                if (locInfo != null && !locInfo.packageDimensions.isEmpty()) {
                    stock.packageDimensions = locInfo.packageDimensions;

                    boolean canFitInAnyPackageType = false;
                    double productVolume = productInfo.length != null && productInfo.breadth != null
                            && productInfo.height != null
                                    ? productInfo.length.doubleValue() * productInfo.breadth.doubleValue()
                                            * productInfo.height.doubleValue()
                                    : 0;
                    double productWeight = productInfo.weightKgs != null ? productInfo.weightKgs.doubleValue() : 0;

                    for (PackagingHelper.PackageDimension pkg : locInfo.packageDimensions) {
                        double pkgVolume = pkg.getVolume();
                        double pkgMaxWeight = pkg.getMaxWeight();
                        boolean fits = pkgVolume >= productVolume && pkgMaxWeight >= productWeight;
                        if (fits) {
                            canFitInAnyPackageType = true;
                            break;
                        }
                    }

                    if (canFitInAnyPackageType || (productInfo.length == null && productInfo.breadth == null
                            && productInfo.height == null)) {
                        PackagingHelper.ProductDimension productDim = new PackagingHelper.ProductDimension(
                                productInfo.length, productInfo.breadth, productInfo.height,
                                productInfo.weightKgs, stock.availableStock);
                        PackagingHelper.PackagingEstimateResult estimate = packagingHelper
                                .calculatePackaging(productDim, locInfo.packageDimensions);
                        stock.maxItemsPackable = estimate.getMaxItemsPackable();

                        if (stock.maxItemsPackable == 0 && canFitInAnyPackageType && stock.availableStock > 0) {
                            boolean hasPackageQuantity = locInfo.packageDimensions.stream()
                                    .anyMatch(p -> p.getAvailableQuantity() > 0);

                            if (!hasPackageQuantity) {
                                stock.maxItemsPackable = stock.availableStock;
                                stock.packagingErrorMessage = "Product fits in package types but no packages available (all have 0 quantity)";
                            } else {
                                if (estimate.getErrorMessage() != null) {
                                    stock.packagingErrorMessage = estimate.getErrorMessage();
                                }
                            }
                        } else if (estimate.getMaxItemsPackable() == 0 && estimate.getErrorMessage() != null) {
                            stock.packagingErrorMessage = estimate.getErrorMessage();
                        }
                    } else {
                        stock.maxItemsPackable = 0;
                        stock.packagingErrorMessage = "Product dimensions/weight exceed all available package limits";
                    }
                } else {
                    stock.maxItemsPackable = 0;
                }
            }
        }

        return locationInfoMap;
    }

    /**
     * Validates that requested quantities can be fulfilled from available stock and
     * packaging at locations.
     */
    private String checkFeasibility(Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, Integer> productQuantities,
            Map<Long, LocationInfo> locationInfoMap) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();

            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) {
                return String.format(ErrorMessages.OrderOptimizationErrorMessages.PRODUCT_NOT_FOUND_FORMAT, productId);
            }

            int totalStock = info.stockByLocation.values().stream()
                    .mapToInt(s -> s.availableStock)
                    .sum();

            int totalPackable = info.stockByLocation.values().stream()
                    .mapToInt(s -> Math.min(s.availableStock, s.maxItemsPackable))
                    .sum();

            boolean hasPackagesConfigured = info.stockByLocation.values().stream()
                    .anyMatch(s -> s.packageDimensions != null && !s.packageDimensions.isEmpty());

            if (!hasPackagesConfigured && locationInfoMap != null) {
                hasPackagesConfigured = info.stockByLocation.keySet().stream()
                        .anyMatch(locationId -> {
                            LocationInfo locInfo = locationInfoMap.get(locationId);
                            return locInfo != null && locInfo.packageDimensions != null
                                    && !locInfo.packageDimensions.isEmpty();
                        });
            }

            boolean hasAvailablePackages = info.stockByLocation.values().stream()
                    .anyMatch(s -> s.packageDimensions != null &&
                            s.packageDimensions.stream().anyMatch(p -> p.getAvailableQuantity() > 0));

            if (!hasAvailablePackages && locationInfoMap != null) {
                hasAvailablePackages = info.stockByLocation.keySet().stream()
                        .anyMatch(locationId -> {
                            LocationInfo locInfo = locationInfoMap.get(locationId);
                            if (locInfo != null && locInfo.packageDimensions != null) {
                                return locInfo.packageDimensions.stream().anyMatch(p -> p.getAvailableQuantity() > 0);
                            }
                            return false;
                        });
            }

            boolean canFitInAnyPackageType = false;
            if (hasPackagesConfigured) {
                double productVolume = info.length != null && info.breadth != null && info.height != null
                        ? info.length.doubleValue() * info.breadth.doubleValue() * info.height.doubleValue()
                        : 0;
                double productWeight = info.weightKgs != null ? info.weightKgs.doubleValue() : 0;

                canFitInAnyPackageType = info.stockByLocation.values().stream()
                        .filter(s -> s.packageDimensions != null)
                        .flatMap(s -> s.packageDimensions.stream())
                        .anyMatch(pkg -> pkg.getVolume() >= productVolume && pkg.getMaxWeight() >= productWeight);

                if (!canFitInAnyPackageType && locationInfoMap != null) {
                    canFitInAnyPackageType = info.stockByLocation.keySet().stream()
                            .anyMatch(locationId -> {
                                LocationInfo locInfo = locationInfoMap.get(locationId);
                                if (locInfo == null || locInfo.packageDimensions == null)
                                    return false;
                                return locInfo.packageDimensions.stream()
                                        .anyMatch(pkg -> pkg.getVolume() >= productVolume
                                                && pkg.getMaxWeight() >= productWeight);
                            });
                }

                if (info.length == null && info.breadth == null && info.height == null) {
                    canFitInAnyPackageType = true;
                }
            }

            String packagingError = info.stockByLocation.values().stream()
                    .filter(s -> s.packagingErrorMessage != null)
                    .map(s -> s.packagingErrorMessage)
                    .findFirst()
                    .orElse(null);

            if (totalPackable < requestedQty) {
                if (totalStock == 0) {
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.INSUFFICIENT_STOCK_ZERO_FORMAT,
                            info.productTitle, requestedQty);
                } else if (!hasPackagesConfigured) {
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.NO_PACKAGES_CONFIGURED_FORMAT,
                            info.productTitle, totalStock, requestedQty);
                } else if (!hasAvailablePackages) {
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.NO_PACKAGES_AVAILABLE_FORMAT,
                            info.productTitle, totalStock, requestedQty);
                } else if (!canFitInAnyPackageType) {
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.PRODUCT_EXCEEDS_PACKAGE_LIMITS_FORMAT,
                            info.productTitle, totalStock, requestedQty);
                } else if (totalStock >= requestedQty && totalPackable == 0) {
                    String errorDetail = packagingError != null ? packagingError
                            : ErrorMessages.OrderOptimizationErrorMessages.NOT_ENOUGH_PACKAGES_FOR_QUANTITY;
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.CANNOT_PACKAGE_WITH_DETAIL_FORMAT,
                            info.productTitle, totalStock, errorDetail, requestedQty);
                } else {
                    return String.format(ErrorMessages.OrderOptimizationErrorMessages.INSUFFICIENT_STOCK_PACKAGING_FORMAT,
                            info.productTitle, requestedQty, totalStock, totalPackable);
                }
            }
        }

        return null;
    }

    /**
     * Generate candidate allocation strategies
     */
    private List<AllocationCandidate> generateCandidates(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {

        List<AllocationCandidate> candidates = new ArrayList<>();

        for (Long locationId : locationInfoMap.keySet()) {
            if (canLocationFulfillAll(locationId, productInfoMap, productQuantities, locationInfoMap)) {
                AllocationCandidate candidate = createSingleLocationCandidate(
                        locationId, productQuantities);
                candidates.add(candidate);
            }
        }

        AllocationCandidate greedyConsolidation = createGreedyConsolidationCandidate(
                productInfoMap, locationInfoMap, productQuantities);
        if (greedyConsolidation.canFulfillOrder) {
            candidates.add(greedyConsolidation);
        }

        AllocationCandidate greedyStock = createGreedyStockCandidate(
                productInfoMap, locationInfoMap, productQuantities);
        if (greedyStock.canFulfillOrder) {
            candidates.add(greedyStock);
        }

        candidates = removeDuplicateCandidates(candidates);

        return candidates;
    }

    private boolean canLocationFulfillAll(Long locationId,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, Integer> productQuantities,
            Map<Long, LocationInfo> locationInfoMap) {
        LocationInfo locInfo = locationInfoMap.get(locationId);
        if (locInfo == null || locInfo.packageDimensions.isEmpty()) {
            return false;
        }

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();

            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null)
                return false;

            LocationStock stock = info.stockByLocation.get(locationId);
            if (stock == null)
                return false;

            int available = Math.min(stock.availableStock, stock.maxItemsPackable);
            if (available < requestedQty)
                return false;
        }
        return true;
    }

    private AllocationCandidate createSingleLocationCandidate(Long locationId,
            Map<Long, Integer> productQuantities) {
        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> productQtys = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            productQtys.put(entry.getKey(), entry.getValue());
        }

        candidate.locationProductQuantities.put(locationId, productQtys);
        candidate.canFulfillOrder = true;

        return candidate;
    }

    private CustomAllocationResult createCustomAllocationCandidate(
            Map<Long, Map<Long, Integer>> customAllocations,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap) {

        AllocationCandidate candidate = new AllocationCandidate();
        List<String> errors = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Integer>> productEntry : customAllocations.entrySet()) {
            Long productId = productEntry.getKey();
            Map<Long, Integer> locationQtys = productEntry.getValue();

            ProductLocationInfo productInfo = productInfoMap.get(productId);
            if (productInfo == null) {
                errors.add("Product ID " + productId + " not found");
                continue;
            }

            for (Map.Entry<Long, Integer> locEntry : locationQtys.entrySet()) {
                Long locationId = locEntry.getKey();
                Integer qty = locEntry.getValue();

                if (qty != null && qty > 0) {
                    LocationInfo locInfo = locationInfoMap.get(locationId);
                    if (locInfo == null) {
                        errors.add(PRODUCT_ERROR_PREFIX + productInfo.productTitle + "': Location ID " + locationId + " not found");
                    } else {
                        LocationStock stock = productInfo.stockByLocation.get(locationId);
                        if (stock == null) {
                            errors.add(PRODUCT_ERROR_PREFIX + productInfo.productTitle + "': Not available at location '" +
                                    locInfo.locationName + "' (no stock mapping exists)");
                        } else if (stock.availableStock < qty) {
                            errors.add(PRODUCT_ERROR_PREFIX + productInfo.productTitle + "': Insufficient stock at '" +
                                    locInfo.locationName + "'. Requested: " + qty + ", Available: " + stock.availableStock);
                        } else if (locInfo.packageDimensions.isEmpty()) {
                            errors.add(PRODUCT_ERROR_PREFIX + productInfo.productTitle + "': No packages available at '" +
                                    locInfo.locationName + "'");
                        } else {
                            int packable = Math.min(stock.availableStock, stock.maxItemsPackable);
                            if (packable < qty) {
                                errors.add(PRODUCT_ERROR_PREFIX + productInfo.productTitle + "': Cannot package " + qty +
                                        " units at '" + locInfo.locationName + "'. Max packable: " + packable +
                                        (stock.packagingErrorMessage != null ? " (" + stock.packagingErrorMessage + ")" : ""));
                            } else {
                                candidate.locationProductQuantities
                                        .computeIfAbsent(locationId, k -> new HashMap<>())
                                        .put(productId, qty);
                            }
                        }
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            return CustomAllocationResult.error(
                    String.format(ErrorMessages.OrderOptimizationErrorMessages.CUSTOM_ALLOCATION_VALIDATION_FAILED_FORMAT,
                            String.join("\n• ", errors)));
        }

        if (candidate.locationProductQuantities.isEmpty()) {
            return CustomAllocationResult
                    .error(ErrorMessages.OrderOptimizationErrorMessages.NO_VALID_ALLOCATIONS_SPECIFIED);
        }

        candidate.canFulfillOrder = true;
        candidate.shortfall = 0;

        return CustomAllocationResult.success(candidate);
    }

    private AllocationCandidate createGreedyConsolidationCandidate(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {

        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> remainingQuantities = new HashMap<>(productQuantities);

        List<Long> sortedLocations = locationInfoMap.keySet().stream()
                .filter(locId -> {
                    LocationInfo locInfo = locationInfoMap.get(locId);
                    return locInfo != null && !locInfo.packageDimensions.isEmpty();
                })
                .sorted((a, b) -> {
                    long countA = productInfoMap.values().stream()
                            .filter(p -> {
                                LocationStock s = p.stockByLocation.get(a);
                                return s != null && Math.min(s.availableStock, s.maxItemsPackable) > 0;
                            })
                            .count();
                    long countB = productInfoMap.values().stream()
                            .filter(p -> {
                                LocationStock s = p.stockByLocation.get(b);
                                return s != null && Math.min(s.availableStock, s.maxItemsPackable) > 0;
                            })
                            .count();
                    return Long.compare(countB, countA);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        for (Long locationId : sortedLocations) {
            Map<Long, Integer> locationAlloc = new HashMap<>();

            for (Map.Entry<Long, Integer> entry : remainingQuantities.entrySet()) {
                Long productId = entry.getKey();
                int remaining = entry.getValue();
                if (remaining > 0) {
                    ProductLocationInfo info = productInfoMap.get(productId);
                    if (info != null) {
                        LocationStock stock = info.stockByLocation.get(locationId);
                        if (stock != null) {
                            int available = Math.min(stock.availableStock, stock.maxItemsPackable);
                            int toAllocate = Math.min(remaining, available);

                            if (toAllocate > 0) {
                                locationAlloc.put(productId, toAllocate);
                                remainingQuantities.put(productId, remaining - toAllocate);
                            }
                        }
                    }
                }
            }

            if (!locationAlloc.isEmpty()) {
                candidate.locationProductQuantities.put(locationId, locationAlloc);
            }
        }

        int totalRemaining = remainingQuantities.values().stream().mapToInt(Integer::intValue).sum();
        candidate.canFulfillOrder = totalRemaining == 0;
        candidate.shortfall = totalRemaining;

        return candidate;
    }

    private AllocationCandidate createGreedyStockCandidate(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {

        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> remainingQuantities = new HashMap<>(productQuantities);

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int remaining = entry.getValue();

            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null)
                continue;

            List<Long> sortedLocs = info.stockByLocation.entrySet().stream()
                    .filter(e -> {
                        LocationInfo locInfo = locationInfoMap.get(e.getKey());
                        return locInfo != null && !locInfo.packageDimensions.isEmpty();
                    })
                    .sorted((a, b) -> {
                        int availA = Math.min(a.getValue().availableStock, a.getValue().maxItemsPackable);
                        int availB = Math.min(b.getValue().availableStock, b.getValue().maxItemsPackable);
                        return Integer.compare(availB, availA);
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Long locationId : sortedLocs) {
                if (remaining <= 0)
                    break;

                LocationStock stock = info.stockByLocation.get(locationId);
                int available = Math.min(stock.availableStock, stock.maxItemsPackable);
                int toAllocate = Math.min(remaining, available);

                if (toAllocate > 0) {
                    candidate.locationProductQuantities
                            .computeIfAbsent(locationId, k -> new HashMap<>())
                            .put(productId, toAllocate);
                    remaining -= toAllocate;
                }
            }

            remainingQuantities.put(productId, remaining);
        }

        int totalRemaining = remainingQuantities.values().stream().mapToInt(Integer::intValue).sum();
        candidate.canFulfillOrder = totalRemaining == 0;
        candidate.shortfall = totalRemaining;

        return candidate;
    }

    private List<AllocationCandidate> removeDuplicateCandidates(List<AllocationCandidate> candidates) {
        Set<String> seen = new HashSet<>();
        List<AllocationCandidate> unique = new ArrayList<>();

        for (AllocationCandidate c : candidates) {
            String key = c.locationProductQuantities.toString();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(c);
            }
        }

        return unique;
    }

    private boolean hasRemainingProducts(List<ProductAllocationTracker> trackers) {
        for (ProductAllocationTracker tracker : trackers) {
            if (tracker.remainingQty > 0)
                return true;
        }
        return false;
    }

    private void reallocateFromUnserviceableLocations(
            AllocationCandidate candidate,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Set<Long> serviceableLocationIds,
            Set<Long> unserviceableLocationIds) {

        Map<Long, Integer> productsToReallocate = new HashMap<>();
        List<Long> locationsToRemove = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Integer>> locEntry : candidate.locationProductQuantities.entrySet()) {

            Long locationId = locEntry.getKey();
            if (unserviceableLocationIds.contains(locationId)) {
                Map<Long, Integer> productQtys = locEntry.getValue();
                for (Map.Entry<Long, Integer> prodEntry : productQtys.entrySet()) {
                    productsToReallocate.merge(prodEntry.getKey(), prodEntry.getValue(), Integer::sum);
                }
                locationsToRemove.add(locationId);
            }
        }

        if (productsToReallocate.isEmpty()) {
            return;
        }

        for (Long locationId : locationsToRemove) {
            candidate.locationProductQuantities.remove(locationId);
        }

        Map<Long, Map<Long, Integer>> existingAllocations = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> locEntry : candidate.locationProductQuantities.entrySet()) {
            existingAllocations.put(locEntry.getKey(), new HashMap<>(locEntry.getValue()));
        }

        for (Map.Entry<Long, Integer> prodEntry : productsToReallocate.entrySet()) {
            Long productId = prodEntry.getKey();
            int qtyToReallocate = prodEntry.getValue();

            ProductLocationInfo productInfo = productInfoMap.get(productId);
            if (productInfo == null)
                continue;

            List<Long> sortedServiceableLocations = serviceableLocationIds.stream()
                    .filter(locId -> {
                        LocationStock stock = productInfo.stockByLocation.get(locId);
                        LocationInfo locInfo = locationInfoMap.get(locId);
                        return stock != null &&
                                Math.min(stock.availableStock, stock.maxItemsPackable) > 0 &&
                                locInfo != null &&
                                !locInfo.packageDimensions.isEmpty();
                    })
                    .sorted((a, b) -> {
                        LocationStock stockA = productInfo.stockByLocation.get(a);
                        LocationStock stockB = productInfo.stockByLocation.get(b);
                        int availA = Math.min(stockA.availableStock, stockA.maxItemsPackable);
                        int availB = Math.min(stockB.availableStock, stockB.maxItemsPackable);
                        return Integer.compare(availB, availA);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Long locationId : sortedServiceableLocations) {
                if (qtyToReallocate <= 0)
                    break;

                LocationStock stock = productInfo.stockByLocation.get(locationId);
                int totalAvailable = Math.min(stock.availableStock, stock.maxItemsPackable);

                int alreadyAllocated = existingAllocations
                        .getOrDefault(locationId, Collections.emptyMap())
                        .getOrDefault(productId, 0);
                int remainingAvailable = totalAvailable - alreadyAllocated;

                if (remainingAvailable > 0) {
                    int toAllocate = Math.min(qtyToReallocate, remainingAvailable);

                    candidate.locationProductQuantities
                            .computeIfAbsent(locationId, k -> new HashMap<>())
                            .merge(productId, toAllocate, Integer::sum);

                    existingAllocations
                            .computeIfAbsent(locationId, k -> new HashMap<>())
                            .merge(productId, toAllocate, Integer::sum);

                    qtyToReallocate -= toAllocate;
                }
            }

            if (qtyToReallocate > 0) {
                candidate.canFulfillOrder = false;
                candidate.shortfall += qtyToReallocate;
            }
        }
    }

    private void evaluateCandidates(
            List<AllocationCandidate> candidates,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            ShipRocketHelper shipRocketHelper,
            String deliveryPostcode,
            boolean isCod,
            boolean isCustomAllocation) {

        try {
            shipRocketHelper.getToken();
        } catch (Exception e) {
            // Continue anyway
        }

        Map<String, BigDecimal> routeMaxWeights = new ConcurrentHashMap<>();
        Set<String> uniquePickupPostcodes = new HashSet<>();

        Set<Long> usedLocationIds = new HashSet<>();
        for (AllocationCandidate candidate : candidates) {
            usedLocationIds.addAll(candidate.locationProductQuantities.keySet());
        }

        for (Long locationId : usedLocationIds) {
            LocationInfo locInfo = locationInfoMap.get(locationId);
            if (locInfo != null && locInfo.postalCode != null) {
                uniquePickupPostcodes.add(locInfo.postalCode);
            }
        }

        List<CompletableFuture<Void>> maxWeightFutures = new ArrayList<>();
        for (String pickupPostcode : uniquePickupPostcodes) {
            final String postcode = pickupPostcode;
            maxWeightFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    double maxWeight = findMaxWeightForRoute(shipRocketHelper, postcode, deliveryPostcode, isCod);
                    if (maxWeight > 0) {
                        routeMaxWeights.put(postcode, BigDecimal.valueOf(maxWeight));
                    } else {
                        routeMaxWeights.put(postcode, BigDecimal.ZERO);
                    }
                } catch (Exception e) {
                    routeMaxWeights.put(postcode, MAX_WEIGHT_PER_SHIPMENT);
                }
            }));
        }

        try {
            CompletableFuture<Void> allMaxWeights = CompletableFuture
                    .allOf(maxWeightFutures.toArray(new CompletableFuture[0]));
            allMaxWeights.get(SHIPPING_API_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            for (String postcode : uniquePickupPostcodes) {
                if (!routeMaxWeights.containsKey(postcode)) {
                    routeMaxWeights.put(postcode, MAX_WEIGHT_PER_SHIPMENT);
                }
            }
        }

        Set<Long> serviceableLocationIds = new HashSet<>();
        Set<Long> unserviceableLocationIds = new HashSet<>();
        for (Long locationId : usedLocationIds) {
            LocationInfo locInfo = locationInfoMap.get(locationId);
            if (locInfo == null)
                continue;

            String pickupPostcode = locInfo.postalCode;
            BigDecimal routeMaxWeight = pickupPostcode != null
                    ? routeMaxWeights.getOrDefault(pickupPostcode, MAX_WEIGHT_PER_SHIPMENT)
                    : MAX_WEIGHT_PER_SHIPMENT;

            if (routeMaxWeight.compareTo(BigDecimal.ZERO) > 0) {
                serviceableLocationIds.add(locationId);
            } else {
                unserviceableLocationIds.add(locationId);
            }
        }

        if (!isCustomAllocation) {
            for (AllocationCandidate candidate : candidates) {
                reallocateFromUnserviceableLocations(candidate, productInfoMap, locationInfoMap,
                        serviceableLocationIds, unserviceableLocationIds);
            }
        }

        for (AllocationCandidate candidate : candidates) {
            List<OrderOptimizationResponseModel.Shipment> allShipments = new ArrayList<>();
            boolean hasUnserviceableRoute = false;
            StringBuilder routeErrors = new StringBuilder();

            for (Map.Entry<Long, Map<Long, Integer>> locEntry : candidate.locationProductQuantities.entrySet()) {

                Long locationId = locEntry.getKey();
                Map<Long, Integer> productQtys = locEntry.getValue();
                LocationInfo locInfo = locationInfoMap.get(locationId);

                if (locInfo != null) {
                    String pickupPostcode = locInfo.postalCode;
                    BigDecimal routeMaxWeight = pickupPostcode != null
                            ? routeMaxWeights.getOrDefault(pickupPostcode, MAX_WEIGHT_PER_SHIPMENT)
                            : MAX_WEIGHT_PER_SHIPMENT;

                    if (routeMaxWeight.compareTo(BigDecimal.ZERO) == 0) {
                        hasUnserviceableRoute = true;
                        String locationName = locInfo.locationName != null ? locInfo.locationName : UNKNOWN_LOCATION_NAME;
                        if (!routeErrors.isEmpty())
                            routeErrors.append("; ");
                        routeErrors.append("No courier options available between pickup location ")
                                .append(locationName).append(" [").append(pickupPostcode).append("]")
                                .append(" and delivery postcode [").append(deliveryPostcode).append("]")
                                .append(" (no alternative locations available)");
                    } else {
                        List<OrderOptimizationResponseModel.ProductAllocation> productAllocations = new ArrayList<>();
                        BigDecimal locationWeight = BigDecimal.ZERO;
                        int locationQty = 0;

                        for (Map.Entry<Long, Integer> prodEntry : productQtys.entrySet()) {
                            Long productId = prodEntry.getKey();
                            int qty = prodEntry.getValue();

                            ProductLocationInfo productInfo = productInfoMap.get(productId);
                            if (productInfo != null) {
                                OrderOptimizationResponseModel.ProductAllocation prodAlloc = new OrderOptimizationResponseModel.ProductAllocation();

                                if (productInfo.productEntity != null) {
                                    prodAlloc.setProduct(new ProductResponseModel(productInfo.productEntity));
                                }
                                prodAlloc.setAllocatedQuantity(qty);
                                prodAlloc.setTotalWeight(productInfo.weightKgs.multiply(BigDecimal.valueOf(qty)));

                                productAllocations.add(prodAlloc);
                                locationWeight = locationWeight.add(prodAlloc.getTotalWeight());
                                locationQty += qty;
                            }
                        }

                        List<OrderOptimizationResponseModel.PackageUsage> packageUsages = calculatePackagingWithDetails(
                                productQtys, productInfoMap, locInfo);

                        List<OrderOptimizationResponseModel.Shipment> locationShipments = splitIntoShipments(locInfo,
                                productAllocations, packageUsages,
                                locationWeight, locationQty, productInfoMap, routeMaxWeight);

                        allShipments.addAll(locationShipments);
                    }
                }
            }

            candidate.shipments = allShipments;

            if (hasUnserviceableRoute) {
                candidate.allCouriersAvailable = false;
                candidate.unavailabilityReason = routeErrors.toString();
            }
        }

        Map<String, CompletableFuture<ShippingOptionsResponseModel>> shippingFutures = new ConcurrentHashMap<>();

        for (AllocationCandidate candidate : candidates) {
            for (OrderOptimizationResponseModel.Shipment shipment : candidate.shipments) {
                if (shipment.getPickupLocation() != null &&
                        shipment.getPickupLocation().getAddress() != null) {
                    String pickupPostcode = shipment.getPickupLocation().getAddress().getPostalCode();
                    if (pickupPostcode != null) {
                        BigDecimal weight = shipment.getTotalWeightKgs().max(BigDecimal.valueOf(0.5));
                        String cacheKey = pickupPostcode + "-" + deliveryPostcode + "-" +
                                weight.setScale(2, RoundingMode.HALF_UP).toString();

                        if (!shippingFutures.containsKey(cacheKey)) {
                            final String finalPickupPostcode = pickupPostcode;
                            final String finalWeight = weight.toString();
                            shippingFutures.put(cacheKey, CompletableFuture.supplyAsync(() -> {
                                try {
                                    return shipRocketHelper.getAvailableShippingOptions(
                                            finalPickupPostcode, deliveryPostcode, isCod, finalWeight);
                                } catch (Exception e) {
                                    return null;
                                }
                            }));
                        }
                    }
                }
            }
        }

        try {
            CompletableFuture<Void> allShipping = CompletableFuture.allOf(
                    shippingFutures.values().toArray(new CompletableFuture[0]));
            allShipping.get(SHIPPING_API_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Continue with available results
        }

        Map<String, ShippingOptionsResponseModel> shippingResults = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<ShippingOptionsResponseModel>> entry : shippingFutures.entrySet()) {
            try {
                ShippingOptionsResponseModel result = entry.getValue().get(1, TimeUnit.SECONDS);
                if (result != null) {
                    shippingResults.put(entry.getKey(), result);
                }
            } catch (Exception e) {
                // Skip failed lookups
            }
        }

        for (AllocationCandidate candidate : candidates) {
            BigDecimal totalPackagingCost = BigDecimal.ZERO;
            BigDecimal totalShippingCost = BigDecimal.ZERO;
            boolean allCouriersAvailable = true;
            StringBuilder unavailabilityReasons = new StringBuilder();

            List<OrderOptimizationResponseModel.Shipment> validShipments = new ArrayList<>();

            for (OrderOptimizationResponseModel.Shipment shipment : candidate.shipments) {
                if (shipment.getPackagesUsed() == null || shipment.getPackagesUsed().isEmpty()) {
                    String locationName = shipment.getPickupLocation() != null
                            ? shipment.getPickupLocation().getAddressNickName()
                            : UNKNOWN_LOCATION_NAME;
                    if (!unavailabilityReasons.isEmpty()) {
                        unavailabilityReasons.append("; ");
                    }
                    unavailabilityReasons.append("No packages available at ").append(locationName)
                            .append(" to fit products (skipped)");
                    continue;
                }

                totalPackagingCost = totalPackagingCost.add(shipment.getPackagingCost());

                String pickupPostcode = shipment.getPickupLocation() != null &&
                        shipment.getPickupLocation().getAddress() != null
                                ? shipment.getPickupLocation().getAddress().getPostalCode()
                                : null;

                if (pickupPostcode != null) {
                    BigDecimal weight = shipment.getTotalWeightKgs().max(BigDecimal.valueOf(0.5));
                    String cacheKey = pickupPostcode + "-" + deliveryPostcode + "-" +
                            weight.setScale(2, RoundingMode.HALF_UP).toString();

                    ShippingOptionsResponseModel shippingOpts = shippingResults.get(cacheKey);
                        if (shippingOpts != null && shippingOpts.getData() != null &&
                            shippingOpts.getData().getAvailableCourierCompanies() != null &&
                            !shippingOpts.getData().getAvailableCourierCompanies().isEmpty()) {

                        shippingOpts.getData().getAvailableCourierCompanies().sort(
                            (a, b) -> Double.compare(a.getRate(), b.getRate()));

                        for (var courier : shippingOpts.getData().getAvailableCourierCompanies()) {
                            shipment.getAvailableCouriers()
                                    .add(ShippingCalculationResponseModel.CourierOption.fromShiprocketCourier(courier));
                        }

                        shipment.setShippingCost(shipment.getAvailableCouriers().get(0).getRate());
                        shipment.setTotalCost(shipment.getPackagingCost().add(shipment.getShippingCost()));
                        totalShippingCost = totalShippingCost.add(shipment.getShippingCost());

                        validShipments.add(shipment);
                    } else {
                        shipment.setTotalCost(shipment.getPackagingCost());
                        allCouriersAvailable = false;
                        String locationName = shipment.getPickupLocation() != null
                                ? shipment.getPickupLocation().getAddressNickName()
                                : UNKNOWN_LOCATION_NAME;
                        if (!unavailabilityReasons.isEmpty()) {
                            unavailabilityReasons.append("; ");
                        }
                        unavailabilityReasons.append("No courier options available between pickup location ")
                                .append(locationName).append(" [").append(pickupPostcode).append("]")
                                .append(" and delivery postcode [").append(deliveryPostcode).append("]");
                        validShipments.add(shipment);
                    }
                } else {
                    shipment.setTotalCost(shipment.getPackagingCost());
                    allCouriersAvailable = false;
                    if (!unavailabilityReasons.isEmpty()) {
                        unavailabilityReasons.append("; ");
                    }
                    unavailabilityReasons.append("Missing postal code for shipment");
                    validShipments.add(shipment);
                }
            }

            validShipments.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));

            candidate.shipments = validShipments;

            if (validShipments.isEmpty()) {
                allCouriersAvailable = false;
                if (!unavailabilityReasons.isEmpty()) {
                    unavailabilityReasons.append("; ");
                }
                unavailabilityReasons.append("No valid shipments - all locations lack suitable packaging");
            }

            candidate.totalPackagingCost = totalPackagingCost;
            candidate.totalShippingCost = totalShippingCost;
            candidate.totalCost = totalPackagingCost.add(totalShippingCost);
            candidate.allCouriersAvailable = allCouriersAvailable;
                candidate.unavailabilityReason = !unavailabilityReasons.isEmpty() ? unavailabilityReasons.toString()
                    : null;
        }
    }

    private List<OrderOptimizationResponseModel.Shipment> splitIntoShipments(
            LocationInfo locInfo,
            List<OrderOptimizationResponseModel.ProductAllocation> productAllocations,
            List<OrderOptimizationResponseModel.PackageUsage> packageUsages,
            BigDecimal totalWeight,
            int totalQty,
            Map<Long, ProductLocationInfo> productInfoMap,
            BigDecimal maxWeightPerShipment) {

        List<OrderOptimizationResponseModel.Shipment> shipments = new ArrayList<>();

        if (totalWeight.compareTo(maxWeightPerShipment) <= 0) {
            OrderOptimizationResponseModel.Shipment shipment = createShipment(
                    locInfo, productAllocations, packageUsages, totalWeight, totalQty);
            shipments.add(shipment);
            return shipments;
        }

        List<ProductAllocationTracker> trackers = new ArrayList<>();
        for (OrderOptimizationResponseModel.ProductAllocation alloc : productAllocations) {
            if (alloc.getProduct() != null) {
                Long productId = alloc.getProduct().getProductId();
                ProductLocationInfo productInfo = productInfoMap.get(productId);
                if (productInfo != null) {
                    ProductAllocationTracker tracker = new ProductAllocationTracker();
                    tracker.productResponseModel = alloc.getProduct();
                    tracker.remainingQty = alloc.getAllocatedQuantity();
                    tracker.weightPerUnit = productInfo.weightKgs;
                    trackers.add(tracker);
                }
            }
        }

        trackers.sort((a, b) -> b.weightPerUnit.compareTo(a.weightPerUnit));

        List<List<OrderOptimizationResponseModel.ProductAllocation>> shipmentProducts = new ArrayList<>();
        List<BigDecimal> shipmentWeights = new ArrayList<>();

        while (hasRemainingProducts(trackers)) {
            List<OrderOptimizationResponseModel.ProductAllocation> currentShipmentProducts = new ArrayList<>();
            BigDecimal currentShipmentWeight = BigDecimal.ZERO;

            for (ProductAllocationTracker tracker : trackers) {
                if (tracker.remainingQty > 0) {
                    BigDecimal remainingCapacity = maxWeightPerShipment.subtract(currentShipmentWeight);
                    int unitsCanFit;

                    if (tracker.weightPerUnit.compareTo(BigDecimal.ZERO) > 0) {
                        unitsCanFit = remainingCapacity.divide(tracker.weightPerUnit, 0, RoundingMode.DOWN).intValue();
                    } else {
                        unitsCanFit = tracker.remainingQty;
                    }

                    if (unitsCanFit <= 0 && currentShipmentWeight.compareTo(BigDecimal.ZERO) == 0) {
                        unitsCanFit = 1;
                    }

                    if (unitsCanFit > 0) {
                        int toAllocate = Math.min(tracker.remainingQty, unitsCanFit);

                        OrderOptimizationResponseModel.ProductAllocation splitAlloc = new OrderOptimizationResponseModel.ProductAllocation();
                        splitAlloc.setProduct(tracker.productResponseModel);
                        splitAlloc.setAllocatedQuantity(toAllocate);
                        splitAlloc.setTotalWeight(tracker.weightPerUnit.multiply(BigDecimal.valueOf(toAllocate)));

                        currentShipmentProducts.add(splitAlloc);
                        currentShipmentWeight = currentShipmentWeight.add(splitAlloc.getTotalWeight());
                        tracker.remainingQty -= toAllocate;
                    }
                }
            }

            if (!currentShipmentProducts.isEmpty()) {
                shipmentProducts.add(currentShipmentProducts);
                shipmentWeights.add(currentShipmentWeight);
            }
        }

        int numShipments = shipmentProducts.size();

        List<List<OrderOptimizationResponseModel.PackageUsage>> shipmentPackages = distributePackages(packageUsages,
                shipmentProducts, numShipments);

        for (int i = 0; i < numShipments; i++) {
            if (shipmentProducts.get(i).isEmpty())
                continue;

            int shipmentQty = shipmentProducts.get(i).stream()
                    .mapToInt(OrderOptimizationResponseModel.ProductAllocation::getAllocatedQuantity).sum();

            OrderOptimizationResponseModel.Shipment shipment = createShipment(
                    locInfo, shipmentProducts.get(i), shipmentPackages.get(i),
                    shipmentWeights.get(i), shipmentQty);

            shipments.add(shipment);
        }

        return shipments;
    }

    private List<List<OrderOptimizationResponseModel.PackageUsage>> distributePackages(
            List<OrderOptimizationResponseModel.PackageUsage> totalPackages,
            List<List<OrderOptimizationResponseModel.ProductAllocation>> shipmentProducts,
            int numShipments) {

        List<List<OrderOptimizationResponseModel.PackageUsage>> result = new ArrayList<>();
        for (int i = 0; i < numShipments; i++) {
            result.add(new ArrayList<>());
        }

        List<Integer> shipmentQuantities = shipmentProducts.stream()
                .map(prods -> prods.stream()
                        .mapToInt(OrderOptimizationResponseModel.ProductAllocation::getAllocatedQuantity).sum())
                .collect(Collectors.toCollection(ArrayList::new));

        int totalQty = shipmentQuantities.stream().mapToInt(Integer::intValue).sum();
        if (totalQty == 0)
            return result;

        for (OrderOptimizationResponseModel.PackageUsage pkg : totalPackages) {
            int remainingPackages = pkg.getQuantityUsed();

            for (int i = 0; i < numShipments && remainingPackages > 0; i++) {
                double proportion = (double) shipmentQuantities.get(i) / totalQty;
                int pkgsForShipment = (int) Math.ceil(pkg.getQuantityUsed() * proportion);
                pkgsForShipment = Math.min(pkgsForShipment, remainingPackages);

                if (pkgsForShipment > 0) {
                    OrderOptimizationResponseModel.PackageUsage splitPkg = new OrderOptimizationResponseModel.PackageUsage();
                    splitPkg.setPackageInfo(pkg.getPackageInfo());
                    splitPkg.setQuantityUsed(pkgsForShipment);

                    BigDecimal pricePerUnit = pkg.getPackageInfo() != null &&
                            pkg.getPackageInfo().getPricePerUnit() != null ? pkg.getPackageInfo().getPricePerUnit()
                                    : BigDecimal.ZERO;
                    splitPkg.setTotalCost(pricePerUnit.multiply(BigDecimal.valueOf(pkgsForShipment)));

                    for (OrderOptimizationResponseModel.ProductAllocation prodAlloc : shipmentProducts.get(i)) {
                        if (prodAlloc.getProduct() != null &&
                                pkg.getProductIds().contains(prodAlloc.getProduct().getProductId())) {
                            OrderOptimizationResponseModel.PackageProductDetail detail = new OrderOptimizationResponseModel.PackageProductDetail();
                            detail.setProductId(prodAlloc.getProduct().getProductId());
                            detail.setQuantity(prodAlloc.getAllocatedQuantity());
                            splitPkg.getProductDetails().add(detail);
                            splitPkg.getProductIds().add(prodAlloc.getProduct().getProductId());
                        }
                    }

                    result.get(i).add(splitPkg);
                    remainingPackages -= pkgsForShipment;
                }
            }
        }

        return result;
    }

    private OrderOptimizationResponseModel.Shipment createShipment(
            LocationInfo locInfo,
            List<OrderOptimizationResponseModel.ProductAllocation> products,
            List<OrderOptimizationResponseModel.PackageUsage> packages,
            BigDecimal weight,
            int qty) {

        OrderOptimizationResponseModel.Shipment shipment = new OrderOptimizationResponseModel.Shipment();

        if (locInfo.pickupLocationEntity != null) {
            PickupLocationResponseModel pickupLocationResponse = new PickupLocationResponseModel(
                    locInfo.pickupLocationEntity);
            pickupLocationResponse.setClient(null);
            shipment.setPickupLocation(pickupLocationResponse);
        }

        shipment.setProducts(new ArrayList<>(products));
        shipment.setPackagesUsed(new ArrayList<>(packages));
        shipment.setTotalWeightKgs(weight);
        shipment.setTotalQuantity(qty);

        BigDecimal packagingCost = packages.stream()
                .map(OrderOptimizationResponseModel.PackageUsage::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        shipment.setPackagingCost(packagingCost);

        return shipment;
    }

    private List<OrderOptimizationResponseModel.PackageUsage> calculatePackagingWithDetails(
            Map<Long, Integer> productQtys,
            Map<Long, ProductLocationInfo> productInfoMap,
            LocationInfo locInfo) {

        List<OrderOptimizationResponseModel.PackageUsage> result = new ArrayList<>();

        Map<Long, PackagingHelper.ProductDimension> productDimensions = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productQtys.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();

            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null)
                continue;

            productDimensions.put(productId, new PackagingHelper.ProductDimension(
                    info.length, info.breadth, info.height, info.weightKgs, qty));
        }

        if (productDimensions.isEmpty()) {
            return result;
        }

        PackagingHelper.MultiProductPackagingResult estimate = packagingHelper
                .calculatePackagingForMultipleProducts(productDimensions, locInfo.packageDimensions);

        for (PackagingHelper.MultiProductPackageUsageResult usage : estimate.getPackagesUsed()) {
            OrderOptimizationResponseModel.PackageUsage pkgUsage = new OrderOptimizationResponseModel.PackageUsage();

            com.example.SpringApi.Models.DatabaseModels.Package pkgEntity = locInfo.packageEntities
                    .get(usage.getPackageId());
            if (pkgEntity != null) {
                pkgUsage.setPackageInfo(new PackageResponseModel(pkgEntity));
            }

            pkgUsage.setQuantityUsed(usage.getQuantityUsed());
            pkgUsage.setTotalCost(usage.getTotalCost());

            for (Map.Entry<Long, Integer> productEntry : usage.getProductQuantities().entrySet()) {
                Long productId = productEntry.getKey();
                int qtyInPackage = productEntry.getValue();

                pkgUsage.getProductIds().add(productId);

                OrderOptimizationResponseModel.PackageProductDetail detail = new OrderOptimizationResponseModel.PackageProductDetail();
                detail.setProductId(productId);
                detail.setQuantity(qtyInPackage);
                pkgUsage.getProductDetails().add(detail);
            }

            result.add(pkgUsage);
        }

        return result;
    }

    private void populateResponseFromCandidate(OrderOptimizationResponseModel response,
            AllocationCandidate candidate,
            boolean allCouriersAvailable) {
        response.setDescription(generateDescription(candidate));
        response.setTotalCost(candidate.totalCost);
        response.setTotalPackagingCost(candidate.totalPackagingCost);
        response.setTotalShippingCost(candidate.totalShippingCost);
        response.setShipmentCount(candidate.shipments.size());
        response.setShipments(candidate.shipments);
        response.setCanFulfillOrder(candidate.canFulfillOrder);
        response.setShortfall(candidate.shortfall);
        response.setAllCouriersAvailable(allCouriersAvailable);
    }

    private String generateDescription(AllocationCandidate candidate) {

        Map<Long, List<OrderOptimizationResponseModel.Shipment>> shipmentsByLocation = new LinkedHashMap<>();
        for (OrderOptimizationResponseModel.Shipment s : candidate.shipments) {
            Long locId = s.getPickupLocation() != null ? s.getPickupLocation().getPickupLocationId() : 0L;
            shipmentsByLocation.computeIfAbsent(locId, k -> new ArrayList<>()).add(s);
        }

        if (shipmentsByLocation.size() == 1) {
            OrderOptimizationResponseModel.Shipment firstShipment = candidate.shipments.get(0);
            String locationName = firstShipment.getPickupLocation() != null
                    ? firstShipment.getPickupLocation().getAddressNickName()
                    : "single location";

            if (candidate.shipments.size() > 1) {
                return "All from " + locationName + " (" + candidate.shipments.size() + " shipments)";
            } else {
                return "All from " + locationName;
            }
        } else {
            return "Split: " + shipmentsByLocation.entrySet().stream()
                    .map(entry -> {
                        List<OrderOptimizationResponseModel.Shipment> locShipments = entry.getValue();
                        String name = locShipments.get(0).getPickupLocation() != null
                                ? locShipments.get(0).getPickupLocation().getAddressNickName()
                                : "Location";
                        int totalItems = locShipments.stream()
                                .mapToInt(OrderOptimizationResponseModel.Shipment::getTotalQuantity).sum();
                        if (locShipments.size() > 1) {
                            return name + " (" + totalItems + " items, " + locShipments.size() + " shipments)";
                        } else {
                            return name + " (" + totalItems + " items)";
                        }
                    })
                    .collect(Collectors.joining(" + "));
        }
    }

    private double findMaxWeightForRoute(
            ShipRocketHelper shipRocketHelper,
            String pickupPostcode,
            String deliveryPostcode,
            boolean isCod) {
        double[] weightsToTry = { 500, 400, 300, 200, 100 };

        for (double weight : weightsToTry) {
            try {
                ShippingOptionsResponseModel response = shipRocketHelper.getAvailableShippingOptions(
                        pickupPostcode, deliveryPostcode, isCod, String.valueOf(weight));

                if (response != null && response.getData() != null &&
                    response.getData().getAvailableCourierCompanies() != null &&
                    !response.getData().getAvailableCourierCompanies().isEmpty()) {
                    return weight;
                }
            } catch (Exception e) {
                // Continue to next weight
            }
        }

        return 0;
    }
}
