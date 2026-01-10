package com.example.SpringApi;

public class ErrorMessages {
    // Common error types for controllers
    public static final String ERROR_BAD_REQUEST = "Bad Request";
    public static final String ERROR_NOT_FOUND = "Not Found";
    public static final String ERROR_INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String ERROR_INVALID_CLIENT_ID = "Invalid client ID";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized";
    
    public static final String InvalidColumn = "Invalid column, the column should be one of the following: ";
    public static final String InvalidAddress = "Invalid address, please check all the address fields entered. Address line 1, address line 2, city, state, zip code are all required fields";
    public static final String InvalidPhone = "Invalid phone number, please check the entered phone number. Phone number should be of 10 numbers";
    public static final String Unauthorized = "You are unauthorized to do this action.";
    public static final String ServerError = "An unexpected error occurred.";
    
    // Common validation error messages
    public static final String INVALID_PAGE_NUMBER = "Page number must be non-negative.";
    public static final String INVALID_PAGE_SIZE = "Page size must be between 1 and 100.";
    public static final String OPERATION_FAILED = "Operation failed due to an unexpected error.";
    public static final String INVALID_REQUEST_DATA = "Invalid request data provided.";
    public static final String INVALID_EMAIL = "Invalid email address provided.";
    public static final String EMAIL_REQUIRED = "Email is required and cannot be empty.";
    public static final String FIRST_NAME_REQUIRED = "First name is required and cannot be empty.";
    public static final String INVALID_LEAD_ID = "Invalid lead ID provided.";
    public static final String LEAD_NOT_FOUND = "Lead not found for the given ID.";

    public static class EventErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid event id provided.";

        // additional error messages
        public static final String ER001 = "Event cannot be null.";
        public static final String ER002 = "Event name is required.";
        public static final String ER003 = "Event description and event start/end times are required, and start time must not be after end time.";
        public static final String ER004 = "Time zone is required and must be a valid time zone.";
        public static final String ER005 = "Event location is required.";
    }

    public static class SupportErrorMessages {
        // Additional Error messages
        public static final String ER001 = "The ticket has been created but there was an error uploading the attachments to the ticket, please try editing the ticket again in sometime.";
    }

    public static class CarrierErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Carrier id provided.";

        // Additional error messages
        public static final String ER001 = "Given User and carrier are not mapped.";
        public static final String ER002 = "No Carriers found for given user.";
        public static final String ER003 = "There was an error fetching the issue types from jira.";
        public static final String ER004 = "There was an error fetching the carrier based on the wildcards.";
        public static final String ER005 = "Wilcard and api access key is required.";
        public static final String ER006 = "Invalid Credentials";
        public static final String ER007 = "Invalid Google Credentials Id";
    }

    public static class ClientErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Client Id.";
        public static final String InvalidRequest = "Client request cannot be null.";
        public static final String InvalidName = "Client name is required and cannot be empty.";
        public static final String InvalidDescription = "Client description is required and cannot be empty.";
        public static final String InvalidSupportEmail = "Client support email is required and cannot be empty.";
        public static final String InvalidWebsite = "Client website is required and cannot be empty.";
        public static final String InvalidSendgridSenderName = "SendGrid sender name is required and cannot be empty.";
        public static final String InvalidLogoUpload = "Failed to upload logo to Firebase.";
    }

    public static class LoginErrorMessages {
        // standard error messages
        public static final String InvalidCredentials = "Invalid Credentials";
        public static final String InvalidId = "Invalid User Id";
        public static final String InvalidEmail = "Invalid User Email";
        public static final String AccountConfirmed = "Account has already been confirmed";
        public static final String InvalidToken = "Invalid token";
        public static final String GoogleUserInconsistency = "User present in google users but not present in system users";
        public static final String AddUser = "There was an error creating the user in the database";
        public static final String Login = "There was an error Logging in";

        // Additional error messages
        public static final String ER001 = "There was an error confirming the user email";
        public static final String ER002 = "Failed to Authenticate User";
        public static final String ER003 = "Cannot Reset a password for Oauth User";
        public static final String ER004 = "There was an error resting the user password";
        public static final String ER005 = "Please Confirm Your Account first";
        public static final String ER006 = "Your account has been locked please reset your password to login";
        public static final String ER007 = "Due to multiple failed attempts your account has been locked please reset your password to unlock your account";
        public static final String ER008 = "Email Exists in System, User has signed up using Oauth";
        public static final String ER009 = "Email Exists in System, User is a customer";
        public static final String ER010 = "Email Exists in System";
        public static final String ER011 = "You Currently do not have any permissions to use the System please contact your admin.";
        public static final String ER012 = "Email and password cannot be null or empty.";
        public static final String ER013 = "Login name, password, first name, last name, phone and date of birth are required in order to sign up.";
        public static final String ER014 = "User email is required in order to reset the password.";
        public static final String ER015 = "User email and api key is required to get the access token.";
        public static final String ER016 = "Please use Oauth to sign in";

    }


    public static class UserErrorMessages{
        // standard error messages
        public static final String Unauthorized = "Current user is not authorized to fetch/update details for the given user";
        public static final String InvalidId = "Invalid User Id";
        public static final String InvalidEmail = "Invalid Email";
        public static final String InvalidUser = "Invalid user provided for creation/modification.";
        public static final String InvalidRequest = "User request cannot be null.";
        public static final String InvalidLoginName = "Login name is required and cannot be empty.";
        public static final String InvalidFirstName = "First name is required and cannot be empty.";
        public static final String InvalidLastName = "Last name is required and cannot be empty.";
        public static final String InvalidPhone = "Phone number is required and cannot be empty.";
        public static final String InvalidRole = "Role is required and cannot be empty.";
        public static final String InvalidDob = "Date of birth is required.";
        public static final String AddUser = "There was an error adding the User";
        public static final String EditUser = "There was an error editing the User";
        public static final String EmailExists = "The given email already exists in the system.";

        // Additional error messages
        public static final String ER001 = "There should be at least one row present in the imported excel sheet";
        public static final String ER002 = "Maximum 100 users can be imported at a time.";
        public static final String ER003 = "No permission set exists for the given user in the db.";
        public static final String ER004 = "Email is required and should be valid.";
        public static final String ER005 = "First name is required.";
        public static final String ER006 = "Last name is required.";
        public static final String ER007 = "User role is required and should be one of the following: .";
        public static final String ER008 = "Date of birth is required and should be valid.";
        public static final String ER009 = "Phone number is required and should be valid.";
        public static final String ER010 = "Failed to upload user profile picture.";
        public static final String ER011 = "Google credentials not found for client.";
    }

    public static class UserGroupErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid Group Id";
        public static final String GroupNameExists = "Group name exists in the system.";

        // Additional error messages
        public static final String ER001 = "One or more group ids is not valid.";
        public static final String ER002 = "User group name is required.";
        public static final String ER003 = "User group description is required";
        public static final String ER004 = "At least one user should be selected to include in the user group.";
    }

    public static class AddressErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid Address Id.";
        public static final String NotFound = "Address not found for the give Id.";

        // Additional error messages
        public static final String ER001 = "Address line 1 is required.";
        public static final String ER002 = "City is required.";
        public static final String ER003 = "State is required.";
        public static final String ER004 = "Zip Code is required.";
        public static final String ER005 = "Country is required.";
        public static final String ER006 = "Invalid address type. Must be one of: HOME, WORK, BILLING, SHIPPING, OFFICE, WAREHOUSE.";
        public static final String ER007 = "Invalid postal code. Must be a 5 or 6 digit number.";

    }

    public static class TodoErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid todo Id.";
        public static final String InvalidRequest = "Todo request cannot be null.";
        public static final String InvalidTask = "Task is required and cannot be empty.";
        public static final String TaskTooLong = "Task cannot exceed 500 characters.";
    }

    public static class MessagesErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid message Id.";

        // Additional error messages
        public static final String ER001 =
                "This message cannot be edited as the email is scheduled within 10 minutes from the current time/";
        public static final String ER002 = "There was an error cancelling the scheduled email.";
        public static final String ER003 = "Message title cannot be empty.";
        public static final String ER004 = "Message description cannot be empty.";
        public static final String ER005 = "Message publish date cannot be empty and needs to be greater than or equal to current date.";
        public static final String ER006 = "Message description markdown cannot be empty";
        public static final String ER007 = "Message description html cannot be empty";
        public static final String ER008 = "Atleast one user/usergroup needs to be present in the message.";
        public static final String ER009 = "Cannot schedule email in the past. Publish date must be in the future.";
        public static final String ER010 = "Cannot schedule email beyond 72 hours. SendGrid only allows scheduling within a 72-hour window from now (UTC timezone).";
        public static final String ER011 = "Cannot edit message. The scheduled email has already been sent (publish date has passed).";
    }

    public static class WebTemplatesErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid web template Id.";
        public static final String UrlExists = "Same Url exists in the db, url should be unique.";

        // Additional error messages
        public static final String ER001 = "At least one sort option should be selected and should be one of the following: \"Price(low to high)\", \"Price(high to low)\", \"Rating\", \"Newest\", \"Oldest\"";
        public static final String ER002 = "At least one product id should be selected, and should be valid.";
        public static final String ER003 = "At least one filter option should be selected and should be one of the following: \"Price Range\", \"Category\", \"Brand\", \"Size\", \"Color\", \"Rating\", \"Availability\"";
        public static final String ER004 = "At least one filter option should be selected and should be one of the following: \"Credit Card\", \"Debit Card\", \"Amazon Pay\", \"Net Banking\", \"UPI\", \"EMI\", \"Gift Cards\", \"Cash on Delivery (COD)\"";
        public static final String ER005 = "Header color is required.";
        public static final String ER006 = "At least one Shipping State is required. A complete list of valid states list can be found here: ";
        public static final String ER007 = "At least one City mapping is required for each state. A complete list of valid state -> city mapping can be found here: ";
        public static final String ER008 = "Url should be present and valid. The url should also be a subdomain of ultimatecompany.com";
        public static final String ER009 = "Card header, card subtext, header - font styles are required. Each font style should have the font style, font color and the font size.";

        public static final String ER010 = "No items are present in the cart for the given userid and productid.";
        public static final String ER011 = "No items are present in the liked items for the given userid and productid.";
    }

    public static class PackageErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Package Id";
        public static final String InvalidRequest = "Package request cannot be null.";
        public static final String InvalidPackageName = "Package name is required and cannot be empty.";
        public static final String InvalidLength = "Package length is required and must be greater than 0.";
        public static final String InvalidBreadth = "Package breadth is required and must be greater than 0.";
        public static final String InvalidHeight = "Package height is required and must be greater than 0.";
        public static final String InvalidMaxWeight = "Package max weight is required and must be greater than or equal to 0.";
        public static final String InvalidStandardCapacity = "Package standard capacity is required and must be greater than 0.";
        public static final String InvalidPricePerUnit = "Package price per unit is required and must be greater than or equal to 0.";
        public static final String InvalidPackageType = "Package type is required and cannot be empty.";
        public static final String InvalidClientId = "Package client ID is required.";

        // Additional error messages
        public static final String ER001 = "Length, breadth and height are required and should be greater than 0";
        public static final String ER002 = "Quantity is required and should be greater than 0";
        public static final String ER003 = "Package with the same dimensions exists in the system, please update the quantity of the same package.";
    }

    public static class PickupLocationErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid pickup location Id.";
        public static final String NotFound = "Pickup location not found with ID: %s";
        public static final String DuplicateName = "Duplicate pickup location name.";
        public static final String InvalidRequest = "Pickup location request cannot be null.";
        public static final String InvalidAddressNickName = "Address nickname is required and cannot be empty.";
        public static final String AddressNickNameTooLong = "Address nickname cannot exceed 255 characters.";
        public static final String InvalidShipRocketId = "ShipRocket pickup location ID must be greater than 0.";

        // Additional error messages
        public static final String ER001 = "Pickup location is required and should be valid.";
        public static final String ER002 = "Email on address is required and should be valid.";
        public static final String ER003 = "Phone on address is required and should be valid.";
    }

    public static class PromoErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid promo Id.";
        public static final String InvalidName = "Invalid promo code.";
        public static final String DuplicateName = "Duplicate promo code name.";
        public static final String OverlappingPromoCode = "A promo with this code already exists during the specified time period.";
        public static final String InvalidRequest = "Promo request cannot be null.";
        public static final String InvalidDescription = "Description is required and cannot be empty.";
        public static final String DescriptionTooLong = "Description cannot exceed 500 characters.";
        public static final String InvalidDiscountValue = "Discount value is required and must be greater than or equal to 0.";
        public static final String InvalidPercentageValue = "Percentage discount cannot exceed 100%.";
        public static final String InvalidPromoCode = "Promo code is required and cannot be empty.";
        public static final String PromoCodeTooLong = "Promo code cannot exceed 100 characters.";
        public static final String InvalidStartDate = "Start date is required.";
        public static final String StartDateMustBeTodayOrFuture = "Start date must be today or in the future.";
        public static final String InvalidExpiryDate = "Expiry date must be today or in the future.";
        public static final String ExpiryDateMustBeAfterStartDate = "Expiry date must be after or equal to start date.";

        // Additional error messages
        public static final String ER001 = "Promo code is required.";
        public static final String ER002 = "Promo description is required.";
        public static final String ER003 = "Promo discount value is required and should be greater than 0.";
    }

    public static class ProductCategoryErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid category Id.";
        public static final String InvalidRequest = "Product category request cannot be null.";
        public static final String InvalidName = "Category name is required and cannot be empty.";
        public static final String InvalidIsEnd = "Category isEnd flag is required.";
    }

    public static class ProductErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid product Id.";
        public static final String InvalidRequest = "Product request cannot be null.";
        public static final String InvalidTitle = "Product title is required and cannot be empty.";
        public static final String InvalidDescription = "Product description is required and cannot be empty.";
        public static final String InvalidBrand = "Product brand is required and cannot be empty.";
        public static final String InvalidColorLabel = "Product color label is required and cannot be empty.";
        public static final String InvalidCondition = "Product condition is required and cannot be empty.";
        public static final String InvalidCountryOfManufacture = "Product country of manufacture is required and cannot be empty.";
        public static final String InvalidPrice = "Product price is required and must be greater than or equal to 0.";
        public static final String InvalidCategoryId = "Product category ID is required.";
        public static final String InvalidClientId = "Product client ID is required.";
        public static final String InvalidPickupLocationId = "Product pickup location ID is required and cannot be 0.";

        // additional error messages
        public static final String ER001 = "Product title is required.";
        public static final String ER002 = "Product Description plain text and html are required.";
        public static final String ER003 = "Product brand is required.";
        public static final String ER004 = "Product country of manufacture is required.";
        public static final String ER005 = "Product main, top, bottom, front, back, right, left and detail images are required and the urls should be valid.";
        public static final String ER006 = "ItemAvailable from date should not be null and should be greater than or equal to todays date.";
        public static final String ER007 = "Product category id should be present";
        public static final String ER008 = "Product category not found with ID: %s";
        public static final String ER009 = "Required image '%s' is missing";
        public static final String ER010 = "Failed to upload %s image to ImgBB";
        public static final String ER011 = "Failed to upload additional image %d to ImgBB";
        public static final String ER012 = "Failed to process image from URL: %s";
        public static final String ER013 = "Product not found with ID: %d";
        public static final String ER014 = "Product IDs list cannot be null or empty";
        public static final String NoPickupLocationsFound = "No pickup locations found for this product.";
    }

    public static class LeadsErrorMessages{
        // standard error messages
        public static final String InvalidId = "Invalid lead id.";

        // additional error messages
        public static final String ER001 = "Lead email is required.";
        public static final String ER002 = "Lead first name is required.";
        public static final String ER003 = "Lead last name is required.";
        public static final String ER004 = "Lead phone number is required.";
        public static final String ER005 = "Assigned Agent user id should be present in the database.";
        public static final String ER006 = "Website should be correctly formatted.";
        public static final String ER007 = "Invalid lead status, the lead status should be one of the following: ";
        public static final String ER008 = "Lead title is required.";
        public static final String ER009 = "Lead request cannot be null.";
        public static final String ER010 = "Invalid email format.";
        public static final String ER011 = "Invalid phone number format.";
        public static final String ER012 = "Client ID is required.";
        public static final String ER013 = "Address is required. Either provide an address object or an existing addressId.";
        public static final String ER014 = "Invalid address data provided.";
        public static final String ER015 = "Created by ID is required.";
        public static final String ER016 = "Company size must be greater than 0 if provided.";
        public static final String ER017 = "Assigned agent ID must be greater than 0 if provided.";
        public static final String ER018 = "Invalid user.";
    }

    public static class PurchaseOrderErrorMessages {
        // Standard error messages for purchase order operations
        public static final String InvalidId = "Invalid purchase order Id.";
        public static final String InvalidRequest = "Purchase order request cannot be null.";
        public static final String InvalidVendorNumber = "Vendor number is required and cannot be empty.";
        public static final String InvalidTermsConditions = "Terms and conditions HTML is required and cannot be empty.";
        public static final String InvalidOrderStatus = "Order status is required and cannot be empty.";
        public static final String InvalidOrderStatusValue = "Invalid purchase order status.";
        public static final String InvalidPaymentStatus = "Payment status is required and cannot be empty.";
        public static final String InvalidTotalAmount = "Total amount is required and must be greater than or equal to 0.";
        public static final String InvalidAmountPaid = "Amount paid is required and must be greater than or equal to 0.";
        public static final String AmountPaidExceedsTotal = "Amount paid cannot exceed total amount.";
        public static final String InvalidClientId = "Purchase order client ID is required.";
        public static final String InvalidLeadId = "Lead ID is required.";
        public static final String InvalidAssignedLeadId = "Assigned lead ID is required.";
        public static final String InvalidAddressId = "Purchase order address ID is required.";
        public static final String InvalidPriority = "Priority is required and must be one of: LOW, MEDIUM, HIGH, URGENT.";
        public static final String AlreadyApproved = "Purchase order is already approved.";
        public static final String AlreadyRejected = "Purchase order is already rejected.";
        public static final String AddressDataRequired = "Address data is required in OrderSummary.";
        public static final String AtLeastOneShipmentRequired = "At least one shipment is required for purchase order.";
        public static final String ImgbbApiKeyNotConfigured = "ImgBB API key is not configured for this client";
        public static final String InvalidAttachmentData = "Each attachment must have a valid fileName (key) and base64 data (value)";
        public static final String FailedToUploadAttachments = "Failed to upload attachments: %s";
        public static final String InvalidColumnName = "Invalid column name: %s";
        public static final String InvalidOperator = "Invalid operator: %s";
        public static final String BooleanColumnsOnlySupportEquals = "Boolean columns only support 'equals' and 'notEquals' operators";
        public static final String ColumnsOnlySupportNumericOperators = "%s columns only support numeric comparison operators";
        public static final String InvalidPagination = "Invalid pagination: end must be greater than start";

        // additional error messages
        public static final String ER001 = "Expected shipment date should be greater than or equal to the current date.";
        public static final String ER002 = "Assigned lead id should be present and cannot be 0";
        public static final String ER003 = "Terms and conditions html value is required.";
        public static final String ER004 = "There should be at least one product, quantity mapping.";
        public static final String ER005 = "Product id should be valid and present in the database and quantity for each product should be greater than 0";
        public static final String ER006 = "Purchase order has already been approved and cannot be approved again by the user";
    }

    public static class ProductReviewErrorMessages {
        // Standard error messages for product review order operations
        public static final String InvalidId = "Invalid product review id.";
        public static final String NotFound = "Product review not found.";

        // additional error messages
        public static final String ER001 = "Product Review ratings should be present and should be between 0 and 5";
        public static final String ER002 = "Product Review text is required.";
        public static final String ER003 = "Product Review user id is required and should be valid.";
        public static final String ER004 = "Product Review product id is required and should be valid.";
    }

    public static class EmailErrorMessages {
        // standard error messages
        public static final String InvalidBatchId = "Invalid Batch Id";

        // additional error messages
        public static final String ER001 = "Failed to send email";
        public static final String ER002 = "Failed to generate batch ID";
        public static final String ER003 = "Failed to cancel email";
    }

    public static class OrderSummaryErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid order summary Id.";
        public static final String InvalidRequest = "OrderSummary data is required.";
        public static final String EntityTypeRequired = "Entity type is required and cannot be empty.";
        public static final String InvalidEntityType = "Invalid entity type. Must be 'PURCHASE_ORDER' or 'ORDER'.";
        public static final String EntityIdRequired = "Entity ID is required and must be greater than 0.";
        public static final String ProductsSubtotalRequired = "productsSubtotal is required.";
        public static final String ProductsSubtotalInvalid = "productsSubtotal must be greater than or equal to 0.";
        public static final String TotalDiscountInvalid = "totalDiscount must be greater than or equal to 0.";
        public static final String PackagingFeeInvalid = "packagingFee must be greater than or equal to 0.";
        public static final String TotalShippingInvalid = "totalShipping must be greater than or equal to 0.";
        public static final String InvalidGstPercentage = "GST percentage must be between 0 and 100.";
        public static final String EntityAddressIdRequired = "Entity address ID is required and must be greater than 0.";
        public static final String PriorityRequired = "Priority is required and cannot be empty.";
        public static final String InvalidPriority = "Invalid priority. Must be 'LOW', 'MEDIUM', 'HIGH', or 'URGENT'.";
        public static final String PaidAmountInvalid = "Paid amount must be greater than or equal to 0.";
        public static final String PaidAmountExceedsGrandTotal = "Paid amount cannot exceed grand total.";
    }

    public static class ShipmentErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid shipment Id.";
        public static final String InvalidRequest = "Shipment data is required.";
        public static final String OrderSummaryIdRequired = "Order summary ID is required and must be greater than 0.";
        public static final String TotalWeightRequired = "Total weight is required.";
        public static final String TotalWeightInvalid = "Total weight must be greater than or equal to 0.";
        public static final String TotalQuantityRequired = "Total quantity is required and must be greater than 0.";
        public static final String PackagingCostRequired = "packagingCost is required.";
        public static final String PackagingCostInvalid = "packagingCost must be greater than or equal to 0.";
        public static final String ShippingCostRequired = "shippingCost is required.";
        public static final String ShippingCostInvalid = "shippingCost must be greater than or equal to 0.";
        public static final String CourierSelectionRequired = "Each shipment must have at least one courier selected.";
    }

    public static class ShipmentProductErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid shipment product Id.";
        public static final String InvalidRequest = "Shipment product data is required.";
        public static final String ShipmentIdRequired = "Shipment ID is required and must be greater than 0.";
        public static final String AllocatedQuantityRequired = "Allocated quantity is required and must be greater than 0.";
        public static final String AllocatedPriceRequired = "Allocated price is required.";
        public static final String AllocatedPriceInvalid = "Allocated price must be greater than or equal to 0.";
    }

    public static class ShipmentPackageErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid shipment package Id.";
        public static final String InvalidRequest = "Shipment package data is required.";
        public static final String ShipmentIdRequired = "Shipment ID is required and must be greater than 0.";
        public static final String QuantityUsedRequired = "Quantity used is required and must be greater than 0.";
        public static final String TotalCostRequired = "Total cost is required.";
        public static final String TotalCostInvalid = "Total cost must be greater than or equal to 0.";
        public static final String AtLeastOnePackageRequired = "Each shipment must have at least one package.";
    }

    public static class ShipmentPackageProductErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid shipment package product Id.";
        public static final String InvalidRequest = "Package product data is required.";
        public static final String ShipmentPackageIdRequired = "Shipment package ID is required and must be greater than 0.";
        public static final String QuantityRequired = "Quantity is required and must be greater than 0.";
        public static final String AtLeastOneProductRequired = "Each package must have at least one product.";
    }

}

