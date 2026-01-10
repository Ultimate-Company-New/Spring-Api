package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.ShippingHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Models.RequestModels.ShipRocketOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.PackageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for processing shipments after payment approval.
 * Handles inventory validation, payment processing, ShipRocket order creation,
 * and inventory updates.
 */
@Service
public class ShipmentProcessingService extends BaseService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderSummaryRepository orderSummaryRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentProductRepository shipmentProductRepository;
    private final ShipmentPackageRepository shipmentPackageRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;
    private final PaymentService paymentService;
    private final PickupLocationRepository pickupLocationRepository;
    private final ProductRepository productRepository;
    private final PackageRepository packageRepository;
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    
    @Autowired
    public ShipmentProcessingService(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderSummaryRepository orderSummaryRepository,
            ShipmentRepository shipmentRepository,
            ShipmentProductRepository shipmentProductRepository,
            ShipmentPackageRepository shipmentPackageRepository,
            ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
            PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
            PaymentService paymentService,
            PickupLocationRepository pickupLocationRepository,
            ProductRepository productRepository,
            PackageRepository packageRepository,
            ClientRepository clientRepository,
            UserLogService userLogService) {
        super();
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderSummaryRepository = orderSummaryRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentProductRepository = shipmentProductRepository;
        this.shipmentPackageRepository = shipmentPackageRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.paymentService = paymentService;
        this.pickupLocationRepository = pickupLocationRepository;
        this.productRepository = productRepository;
        this.packageRepository = packageRepository;
        this.clientRepository = clientRepository;
        this.userLogService = userLogService;
    }
    
    /**
     * Processes shipments after cash payment approval.
     * This is the main entry point that orchestrates the entire flow:
     * 1. Validates product and package availability
     * 2. Processes cash payment
     * 3. Updates inventory (reduces quantities)
     * 4. Creates ShipRocket orders
     * 5. Updates shipments with ShipRocket response
     * 6. Updates PO status
     * 
     * @param purchaseOrderId The purchase order ID
     * @param cashPaymentRequest The cash payment request
     * @return PaymentVerificationResponseModel with success status
     */
    @Transactional
    public PaymentVerificationResponseModel processShipmentsAfterPaymentApproval(
            Long purchaseOrderId,
            CashPaymentRequestModel cashPaymentRequest) {
        
        // Step 1: Validate purchase order exists and is in valid status
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId));
        
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder);
        }
        
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid);
        }
        
        // Get order summary
        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                    OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.OrderSummaryNotFoundMessage.NotFound));
        
        // Step 2: Validate product and package availability at each location
        List<Shipment> shipments = shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
        if (shipments == null || shipments.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound);
        }
        
        validateProductAndPackageAvailability(shipments);
        
        // Step 3: Process cash payment
        PaymentVerificationResponseModel paymentResponse = paymentService.recordCashPayment(cashPaymentRequest);
        
        if (!paymentResponse.isSuccess()) {
            throw new BadRequestException(ErrorMessages.OPERATION_FAILED + " " + paymentResponse.getMessage());
        }
        
        // Step 4: Update inventory (reduce product and package quantities)
        updateInventory(shipments);
        
        // Step 5: Create ShipRocket orders and update shipments
        createShipRocketOrdersAndUpdateShipments(shipments, orderSummary, purchaseOrder);
        
        // Note: PO status is already updated by PaymentService during payment processing
        // No need to update again here
        
        // Log success
        userLogService.logData(
                getUserId(),
                "Shipments processed successfully for PO #" + purchaseOrderId +
                " (" + purchaseOrder.getVendorNumber() + "). Status: " + purchaseOrder.getPurchaseOrderStatus(),
                "ShipmentProcessing/processShipmentsAfterPaymentApproval"
        );
        
        return paymentResponse;
    }
    
    /**
     * Processes shipments after online payment approval.
     * This is the main entry point that orchestrates the entire flow:
     * 1. Validates product and package availability
     * 2. Verifies online payment
     * 3. Updates inventory (reduces quantities)
     * 4. Creates ShipRocket orders
     * 5. Updates shipments with ShipRocket response
     * 6. Updates PO status
     * 
     * @param purchaseOrderId The purchase order ID
     * @param razorpayVerifyRequest The Razorpay payment verification request
     * @return PaymentVerificationResponseModel with success status
     */
    @Transactional
    public PaymentVerificationResponseModel processShipmentsAfterPaymentApproval(
            Long purchaseOrderId,
            RazorpayVerifyRequestModel razorpayVerifyRequest) {
        
        // Step 1: Validate purchase order exists and is in valid status
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.PurchaseOrderErrorMessages.InvalidId));
        
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder);
        }
        
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid);
        }
        
        // Get order summary
        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                    OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrderId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.OrderSummaryNotFoundMessage.NotFound));
        
        // Step 2: Validate product and package availability at each location
        List<Shipment> shipments = shipmentRepository.findByOrderSummaryId(orderSummary.getOrderSummaryId());
        if (shipments == null || shipments.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound);
        }
        
        validateProductAndPackageAvailability(shipments);
        
        // Step 3: Verify online payment
        PaymentVerificationResponseModel paymentResponse = paymentService.verifyPayment(razorpayVerifyRequest);
        
        if (!paymentResponse.isSuccess()) {
            throw new BadRequestException(ErrorMessages.OPERATION_FAILED + " " + paymentResponse.getMessage());
        }
        
        // Step 4: Update inventory (reduce product and package quantities)
        updateInventory(shipments);
        
        // Step 5: Create ShipRocket orders and update shipments
        createShipRocketOrdersAndUpdateShipments(shipments, orderSummary, purchaseOrder);
        
        // Note: PO status is already updated by PaymentService during payment processing
        // No need to update again here
        
        // Log success
        userLogService.logData(
                getUserId(),
                "Shipments processed successfully for PO #" + purchaseOrderId +
                " (" + purchaseOrder.getVendorNumber() + "). Status: " + purchaseOrder.getPurchaseOrderStatus(),
                "ShipmentProcessing/processShipmentsAfterPaymentApproval"
        );
        
        return paymentResponse;
    }
    
    /**
     * Validates product and package availability at each pickup location.
     */
    private void validateProductAndPackageAvailability(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            Long pickupLocationId = shipment.getPickupLocationId();
            
            // Validate products
            List<ShipmentProduct> shipmentProducts = shipmentProductRepository.findByShipmentId(shipment.getShipmentId());
            for (ShipmentProduct shipmentProduct : shipmentProducts) {
                ProductPickupLocationMapping mapping = productPickupLocationMappingRepository
                        .findByProductIdAndPickupLocationId(
                                shipmentProduct.getProductId(), 
                                pickupLocationId)
                        .orElseThrow(() -> new BadRequestException(
                                "Product ID " + shipmentProduct.getProductId() +
                                " is not available at pickup location ID " + pickupLocationId));
                
                if (mapping.getAvailableStock() < shipmentProduct.getAllocatedQuantity()) {
                    throw new BadRequestException(
                            "Insufficient stock for product ID " + shipmentProduct.getProductId() +
                            " at pickup location ID " + pickupLocationId +
                            ". Available: " + mapping.getAvailableStock() +
                            ", Requested: " + shipmentProduct.getAllocatedQuantity());
                }
            }
            
            // Validate packages
            List<ShipmentPackage> shipmentPackages = shipmentPackageRepository.findByShipmentId(shipment.getShipmentId());
            for (ShipmentPackage shipmentPackage : shipmentPackages) {
                PackagePickupLocationMapping mapping = packagePickupLocationMappingRepository
                        .findByPackageIdAndPickupLocationId(
                                shipmentPackage.getPackageId(), 
                                pickupLocationId)
                        .orElseThrow(() -> new BadRequestException(
                                "Package ID " + shipmentPackage.getPackageId() +
                                " is not available at pickup location ID " + pickupLocationId));
                
                if (mapping.getAvailableQuantity() < shipmentPackage.getQuantityUsed()) {
                    throw new BadRequestException(
                            "Insufficient packages for package ID " + shipmentPackage.getPackageId() +
                            " at pickup location ID " + pickupLocationId +
                            ". Available: " + mapping.getAvailableQuantity() +
                            ", Requested: " + shipmentPackage.getQuantityUsed());
                }
            }
        }
    }
    
    /**
     * Updates inventory by reducing product and package quantities.
     */
    private void updateInventory(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            Long pickupLocationId = shipment.getPickupLocationId();
            
            // Update product quantities
            List<ShipmentProduct> shipmentProducts = shipmentProductRepository.findByShipmentId(shipment.getShipmentId());
            for (ShipmentProduct shipmentProduct : shipmentProducts) {
                ProductPickupLocationMapping mapping = productPickupLocationMappingRepository
                        .findByProductIdAndPickupLocationId(
                                shipmentProduct.getProductId(), 
                                pickupLocationId)
                        .orElse(null);
                
                if (mapping != null) {
                    int newStock = mapping.getAvailableStock() - shipmentProduct.getAllocatedQuantity();
                    mapping.setAvailableStock(newStock);
                    mapping.setLastStockUpdate(LocalDateTime.now());
                    mapping.setModifiedUser(getUser());
                    productPickupLocationMappingRepository.save(mapping);
                }
            }
            
            // Update package quantities
            List<ShipmentPackage> shipmentPackages = shipmentPackageRepository.findByShipmentId(shipment.getShipmentId());
            for (ShipmentPackage shipmentPackage : shipmentPackages) {
                PackagePickupLocationMapping mapping = packagePickupLocationMappingRepository
                        .findByPackageIdAndPickupLocationId(
                                shipmentPackage.getPackageId(), 
                                pickupLocationId)
                        .orElse(null);
                
                if (mapping != null) {
                    int newQuantity = mapping.getAvailableQuantity() - shipmentPackage.getQuantityUsed();
                    mapping.setAvailableQuantity(newQuantity);
                    mapping.setModifiedUser(getUser());
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
            PurchaseOrder purchaseOrder) {
        
        // Get ShipRocket credentials from client
        Long clientId = getClientId();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
        
        if (client.getShipRocketEmail() == null || client.getShipRocketEmail().trim().isEmpty() ||
            client.getShipRocketPassword() == null || client.getShipRocketPassword().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured);
        }
        
        ShippingHelper shippingHelper = new ShippingHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
        
        // Get delivery address
        Address deliveryAddress = orderSummary.getEntityAddress();
        if (deliveryAddress == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.DeliveryAddressNotFound);
        }
        
        // Get pickup location details (need ShipRocket pickup location ID)
        Map<Long, PickupLocation> pickupLocationMap = new HashMap<>();
        for (Shipment shipment : shipments) {
            PickupLocation pickupLocation = pickupLocationRepository.findById(shipment.getPickupLocationId())
                    .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, shipment.getPickupLocationId())));
            pickupLocationMap.put(shipment.getPickupLocationId(), pickupLocation);
        }
        
        // Process each shipment
        for (Shipment shipment : shipments) {
            PickupLocation pickupLocation = pickupLocationMap.get(shipment.getPickupLocationId());
            
            // Build ShipRocket order request
            ShipRocketOrderRequestModel orderRequest = buildShipRocketOrderRequest(
                    shipment, orderSummary, purchaseOrder, deliveryAddress, pickupLocation);
            
            // Create ShipRocket order
            ShipRocketOrderResponseModel shipRocketResponse = shippingHelper.createCustomOrder(orderRequest);
            
            // Validate ShipRocket response - ShipRocket can return 200 with error messages
            // Success response must have order_id, shipment_id, status, and status_code
            validateShipRocketOrderResponse(shipRocketResponse, shipment.getShipmentId());
            
            // Update shipment with ShipRocket response
            updateShipmentWithShipRocketResponse(shipment, shipRocketResponse, shippingHelper);
        }
    }
    
    /**
     * Builds ShipRocket order request model.
     * 
     * IMPORTANT: ShipRocket expects pickup_location to be the NAME (string) of the pickup location,
     * not the ID. The name must match exactly what was used when creating the pickup location in ShipRocket.
     */
    private ShipRocketOrderRequestModel buildShipRocketOrderRequest(
            Shipment shipment,
            OrderSummary orderSummary,
            PurchaseOrder purchaseOrder,
            Address deliveryAddress,
            PickupLocation pickupLocation) {
        
        ShipRocketOrderRequestModel request = new ShipRocketOrderRequestModel();
        
        // Get client for company name
        Client client = clientRepository.findById(getClientId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
        
        // Order ID (required) - Format: PO_{purchaseOrderId}
        request.setOrderId("PO_" + purchaseOrder.getPurchaseOrderId());
        
        // Order date (required) - Format: yyyy-MM-dd HH:mm
        request.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        // Pickup location (REQUIRED) - Must be the NAME (string), not the ID
        if (pickupLocation.getAddressNickName() == null || pickupLocation.getAddressNickName().trim().isEmpty()) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.PickupLocationNameNotConfigured, 
                    pickupLocation.getPickupLocationId()));
        }
        request.setPickupLocation(pickupLocation.getAddressNickName().trim());
        
        // Channel ID (optional)
        request.setChannelId("");
        
        // Company name (optional)
        if (client.getName() != null && !client.getName().trim().isEmpty()) {
            request.setCompanyName(client.getName().trim());
        }
        
        // Comment/Reseller name (optional)
        if (purchaseOrder.getVendorNumber() != null && !purchaseOrder.getVendorNumber().trim().isEmpty()) {
            String vendorInfo = "Vendor: " + purchaseOrder.getVendorNumber().trim();
            request.setComment(vendorInfo);
            request.setResellerName(vendorInfo);
        }
        
        // Billing address (required fields)
        String[] nameParts = splitName(deliveryAddress.getNameOnAddress());
        request.setBillingCustomerName(nameParts[0]);
        if (nameParts.length > 1 && !nameParts[1].isEmpty()) {
            request.setBillingLastName(nameParts[1]);
        }
        request.setBillingAddress(deliveryAddress.getStreetAddress() != null ? deliveryAddress.getStreetAddress() : "");
        if (deliveryAddress.getStreetAddress2() != null && !deliveryAddress.getStreetAddress2().trim().isEmpty()) {
            request.setBillingAddress2(deliveryAddress.getStreetAddress2());
        }
        request.setBillingCity(deliveryAddress.getCity() != null ? deliveryAddress.getCity() : "");
        
        // Parse postal code to integer
        try {
            request.setBillingPincode(Integer.parseInt(deliveryAddress.getPostalCode()));
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.BillingPostalCodeMustBeNumeric, deliveryAddress.getPostalCode()));
        }
        
        request.setBillingState(deliveryAddress.getState() != null ? deliveryAddress.getState() : "");
        request.setBillingCountry(deliveryAddress.getCountry() != null ? deliveryAddress.getCountry() : "");
        request.setBillingEmail(deliveryAddress.getEmailOnAddress() != null ? deliveryAddress.getEmailOnAddress() : "");
        
        // Clean phone number (remove formatting, keep only digits, ensure 10 digits)
        String billingPhone = cleanPhoneNumber(deliveryAddress.getPhoneOnAddress());
        if (billingPhone.length() != 10) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.BillingPhoneMustBe10Digits, 
                    (deliveryAddress.getPhoneOnAddress() != null ? deliveryAddress.getPhoneOnAddress() : "empty")));
        }
        request.setBillingPhone(Long.parseLong(billingPhone));
        
        // Billing ISD code (optional) - Default to +91 for India
        request.setBillingIsdCode("+91");
        
        // Shipping address (same as billing)
        request.setShippingIsBilling(true);
        request.setShippingCustomerName(nameParts[0]);
        if (nameParts.length > 1 && !nameParts[1].isEmpty()) {
            request.setShippingLastName(nameParts[1]);
        }
        request.setShippingAddress(deliveryAddress.getStreetAddress() != null ? deliveryAddress.getStreetAddress() : "");
        if (deliveryAddress.getStreetAddress2() != null && !deliveryAddress.getStreetAddress2().trim().isEmpty()) {
            request.setShippingAddress2(deliveryAddress.getStreetAddress2());
        }
        request.setShippingCity(deliveryAddress.getCity() != null ? deliveryAddress.getCity() : "");
        
        // Parse shipping postal code to integer
        try {
            request.setShippingPincode(Integer.parseInt(deliveryAddress.getPostalCode()));
        } catch (NumberFormatException e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShippingPostalCodeMustBeNumeric, deliveryAddress.getPostalCode()));
        }
        
        request.setShippingState(deliveryAddress.getState() != null ? deliveryAddress.getState() : "");
        request.setShippingCountry(deliveryAddress.getCountry() != null ? deliveryAddress.getCountry() : "");
        if (deliveryAddress.getEmailOnAddress() != null && !deliveryAddress.getEmailOnAddress().trim().isEmpty()) {
            request.setShippingEmail(deliveryAddress.getEmailOnAddress());
        }
        
        // Clean shipping phone number
        String shippingPhone = cleanPhoneNumber(deliveryAddress.getPhoneOnAddress());
        if (shippingPhone.length() != 10) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShippingPhoneMustBe10Digits, 
                    (deliveryAddress.getPhoneOnAddress() != null ? deliveryAddress.getPhoneOnAddress() : "empty")));
        }
        request.setShippingPhone(Long.parseLong(shippingPhone));
        
        // Order items (required)
        List<ShipRocketOrderRequestModel.OrderItem> orderItems = new ArrayList<>();
        List<ShipmentProduct> shipmentProducts = shipmentProductRepository.findByShipmentId(shipment.getShipmentId());
        for (ShipmentProduct shipmentProduct : shipmentProducts) {
            Product product = productRepository.findById(shipmentProduct.getProductId()).orElse(null);
            if (product != null) {
                ShipRocketOrderRequestModel.OrderItem item = new ShipRocketOrderRequestModel.OrderItem();
                item.setName(product.getTitle() != null ? product.getTitle() : "");
                item.setSku(product.getUpc() != null && !product.getUpc().trim().isEmpty() ? 
                        product.getUpc() : "SKU-" + product.getProductId());
                item.setUnits(shipmentProduct.getAllocatedQuantity());
                item.setSellingPrice((int) Math.round(shipmentProduct.getAllocatedPrice().doubleValue()));
                
                // Discount (optional)
                if (product.getDiscount() != null && product.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = product.getIsDiscountPercent() != null && product.getIsDiscountPercent() ?
                            shipmentProduct.getAllocatedPrice().multiply(product.getDiscount()).divide(new BigDecimal("100")) :
                            product.getDiscount();
                    item.setDiscount((int) Math.round(discountAmount.doubleValue()));
                }
                
                // Tax/GST (optional)
                if (orderSummary.getGstPercentage() != null) {
                    item.setTax(orderSummary.getGstPercentage().intValue());
                }
                
                orderItems.add(item);
            }
        }
        request.setOrderItems(orderItems);
        
        // Payment method (required)
        request.setPaymentMethod("prepaid");
        
        // Shipping charges (optional)
        if (orderSummary.getTotalShipping() != null && orderSummary.getTotalShipping().compareTo(BigDecimal.ZERO) > 0) {
            request.setShippingCharges((int) Math.round(orderSummary.getTotalShipping().doubleValue()));
        }
        
        // Total discount (optional)
        if (orderSummary.getTotalDiscount() != null && orderSummary.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0) {
            request.setTotalDiscount((int) Math.round(orderSummary.getTotalDiscount().doubleValue()));
        }
        
        // Subtotal (required)
        request.setSubTotal((int) Math.round(orderSummary.getSubtotal().doubleValue()));
        
        // Package dimensions (required) - Calculate sum total from all packages
        int totalLength = 0;
        int totalBreadth = 0;
        int totalHeight = 0;
        
        List<ShipmentPackage> shipmentPackages = shipmentPackageRepository.findByShipmentId(shipment.getShipmentId());
        for (ShipmentPackage shipmentPackage : shipmentPackages) {
            com.example.SpringApi.Models.DatabaseModels.Package packageEntity = packageRepository.findById(shipmentPackage.getPackageId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId + " ID: " + shipmentPackage.getPackageId()));
            
            totalLength += packageEntity.getLength() * shipmentPackage.getQuantityUsed();
            totalBreadth += packageEntity.getBreadth() * shipmentPackage.getQuantityUsed();
            totalHeight += packageEntity.getHeight() * shipmentPackage.getQuantityUsed();
        }
        
        request.setLength(totalLength > 0 ? (double) totalLength : 10.0);
        request.setBreadth(totalBreadth > 0 ? (double) totalBreadth : 10.0);
        request.setHeight(totalHeight > 0 ? (double) totalHeight : 10.0);
        request.setWeight(shipment.getTotalWeightKgs().doubleValue());
        
        // COD amount (required) - 0 for prepaid
        request.setCodAmount(0.0);
        
        // Courier ID (required)
        request.setCourierId(shipment.getSelectedCourierCompanyId());
        
        // Invoice number (optional)
        if (purchaseOrder.getPurchaseOrderReceipt() != null && !purchaseOrder.getPurchaseOrderReceipt().trim().isEmpty()) {
            request.setInvoiceNumber(purchaseOrder.getPurchaseOrderReceipt().trim());
        }
        
        // Insurance option (optional)
        request.setIsInsuranceOpt(false);
        
        // Is document (optional)
        request.setIsDocument(0);
        
        // Order tag (optional)
        if (purchaseOrder.getVendorNumber() != null && !purchaseOrder.getVendorNumber().trim().isEmpty()) {
            request.setOrderTag(purchaseOrder.getVendorNumber().trim());
        }
        
        return request;
    }
    
    /**
     * Helper method to split a full name into first name and last name.
     * 
     * @param fullName The full name string
     * @return Array with [firstName, lastName] - lastName may be empty string
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String trimmed = fullName.trim();
        int spaceIndex = trimmed.indexOf(" ");
        if (spaceIndex > 0 && spaceIndex < trimmed.length() - 1) {
            return new String[]{
                trimmed.substring(0, spaceIndex),
                trimmed.substring(spaceIndex + 1)
            };
        }
        return new String[]{trimmed, ""};
    }
    
    /**
     * Validates ShipRocket order creation response.
     * ShipRocket can return HTTP 200 even with errors, so we must check the response structure.
     * 
     * A successful response must have:
     * - order_id (not null)
     * - shipment_id (not null)
     * - status (not null, typically "NEW")
     * 
     * @param shipRocketResponse The response from ShipRocket API
     * @param shipmentId Our internal shipment ID for error context
     * @throws BadRequestException if the response indicates failure
     */
    private void validateShipRocketOrderResponse(
            ShipRocketOrderResponseModel shipRocketResponse,
            Long shipmentId) {
        
        if (shipRocketResponse == null) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketApiNullResponse, shipmentId));
        }
        
        // Check for error message in response
        if (shipRocketResponse.getMessage() != null && !shipRocketResponse.getMessage().trim().isEmpty()) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed, shipmentId, shipRocketResponse.getMessage()));
        }
        
        // Validate required success fields
        if (shipRocketResponse.getOrder_id() == null) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed, shipmentId, "order_id is missing from response"));
        }
        
        if (shipRocketResponse.getShipment_id() == null) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed, shipmentId, "shipment_id is missing from response"));
        }
        
        // Status should be present (typically "NEW" for successful creation)
        if (shipRocketResponse.getStatus() == null || shipRocketResponse.getStatus().trim().isEmpty()) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed, shipmentId, "status is missing from response"));
        }
        
        // Validate that the status is a valid ShipRocket status
        if (!Shipment.ShipRocketStatus.isValid(shipRocketResponse.getStatus())) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed, shipmentId, 
                    "invalid status '" + shipRocketResponse.getStatus() + "'. Valid statuses are: " +
                    String.join(", ", 
                        java.util.Arrays.stream(Shipment.ShipRocketStatus.values())
                            .map(Shipment.ShipRocketStatus::getValue)
                            .toArray(String[]::new)
                    )));
        }
    }
    
    /**
     * Updates shipment with ShipRocket response data.
     * 
     * @param shipment The shipment to update
     * @param shipRocketResponse The ShipRocket order creation response
     * @param shippingHelper The ShippingHelper instance to fetch order details
     */
    private void updateShipmentWithShipRocketResponse(
            Shipment shipment,
            ShipRocketOrderResponseModel shipRocketResponse,
            ShippingHelper shippingHelper) {
        
        // Validation should have been done before calling this method
        // Populate shipment with ShipRocket order response data
        shipment.populateFromShipRocketOrderResponse(shipRocketResponse);
        String shipRocketOrderId = shipRocketResponse.getOrderIdAsString();
        
        // Step 2: Assign AWB (Air Waybill) to the shipment
        // This generates a tracking number for the shipment
        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipment_id();
            Long courierId = shipment.getSelectedCourierCompanyId();
            
            if (shipRocketShipmentId != null && courierId != null) {
                // Get AWB assignment response as raw JSON and store it
                String awbMetadataJson = shippingHelper.assignAwbAsJson(shipRocketShipmentId, courierId);
                shipment.setShipRocketAwbMetadata(awbMetadataJson);
                
                // Parse the JSON to extract the AWB code using Gson
                com.nimbusds.jose.shaded.gson.Gson gson = new com.nimbusds.jose.shaded.gson.Gson();
                com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel awbResponse = 
                    gson.fromJson(awbMetadataJson, com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel.class);
                
                // Extract AWB code from response
                if (awbResponse != null && awbResponse.isSuccess() && awbResponse.getAwbCode() != null) {
                    shipment.setShipRocketAwbCode(awbResponse.getAwbCode());
                } else {
                    // Fallback to AWB from order creation if AWB assignment doesn't return one
                    shipment.setShipRocketAwbCode(shipRocketResponse.getAwb_code());
                }
            } else {
                // If we can't assign AWB, use the one from order creation response
                shipment.setShipRocketAwbCode(shipRocketResponse.getAwb_code());
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.AwbAssignmentFailed, shipRocketResponse.getShipment_id(), e.getMessage()));
        }
        
        // Step 3: Generate pickup for the shipment
        // This schedules a pickup with the courier after AWB has been assigned
        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipment_id();
            if (shipRocketShipmentId != null) {
                String pickupMetadataJson = shippingHelper.generatePickupAsJson(shipRocketShipmentId);
                shipment.setShipRocketPickupMetadata(pickupMetadataJson);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.PickupGenerationFailed, shipRocketResponse.getShipment_id(), e.getMessage()));
        }
        
        // Step 4: Generate manifest for the shipment
        // This creates a manifest PDF for the shipment
        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipment_id();
            if (shipRocketShipmentId != null) {
                String manifestUrl = shippingHelper.generateManifest(shipRocketShipmentId);
                shipment.setShipRocketGeneratedManifestUrl(manifestUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.ManifestGenerationFailed, shipRocketResponse.getShipment_id(), e.getMessage()));
        }
        
        // Step 5: Generate shipping label for the shipment
        // This creates a shipping label PDF for the shipment
        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipment_id();
            if (shipRocketShipmentId != null) {
                String labelUrl = shippingHelper.generateLabel(shipRocketShipmentId);
                shipment.setShipRocketGeneratedLabelUrl(labelUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.LabelGenerationFailed, shipRocketResponse.getShipment_id(), e.getMessage()));
        }
        
        // Step 6: Generate invoice for the shipment
        // This creates an invoice PDF for the shipment
        try {
            Long shipRocketShipmentId = shipRocketResponse.getShipment_id();
            if (shipRocketShipmentId != null) {
                String invoiceUrl = shippingHelper.generateInvoice(shipRocketShipmentId);
                shipment.setShipRocketGeneratedInvoiceUrl(invoiceUrl);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.InvoiceGenerationFailed, shipRocketResponse.getShipment_id(), e.getMessage()));
        }
        
        // Step 7: Get tracking information for the shipment
        // This retrieves tracking status and activities using the AWB code
        try {
            String awbCode = shipment.getShipRocketAwbCode();
            if (awbCode != null && !awbCode.trim().isEmpty()) {
                String trackingJson = shippingHelper.getTrackingAsJson(awbCode);
                shipment.setShipRocketTrackingMetadata(trackingJson);
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ShippingErrorMessages.TrackingFetchFailed, shipment.getShipRocketAwbCode(), e.getMessage()));
        }
        
        // Step 8: Fetch complete order details from ShipRocket and store as metadata
        // This provides comprehensive order information including AWB data, charges, etc.
        try {
            String orderDetailsJson = shippingHelper.getOrderDetailsAsJson(shipRocketOrderId);
            shipment.setShipRocketFullResponse(orderDetailsJson);
        } catch (Exception e) {
            // If fetching order details fails, store the creation response instead
            // This ensures we always have some metadata stored
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String fullResponseJson = objectMapper.writeValueAsString(shipRocketResponse);
                shipment.setShipRocketFullResponse(fullResponseJson);
            } catch (Exception serializationEx) {
                BadRequestException exception = new BadRequestException("Failed to serialize ShipRocket response to JSON for shipment ID " + 
                        shipment.getShipmentId() + ": " + serializationEx.getMessage());
                exception.initCause(serializationEx);
                throw exception;
            }
        }
        
        shipment.setModifiedUser(getUser());
        shipmentRepository.save(shipment);
    }
    
    /**
     * Cleans phone number by removing formatting and ensuring it's exactly 10 digits.
     * 
     * @param phone The phone number string (may contain formatting like parentheses, dashes, spaces)
     * @return Cleaned phone number with only digits, or empty string if null/empty
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        // Remove all non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        // Ensure it's exactly 10 digits (remove country code if present)
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }
        return cleaned;
    }
}

