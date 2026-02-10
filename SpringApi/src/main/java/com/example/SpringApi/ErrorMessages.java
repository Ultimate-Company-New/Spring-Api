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
        /** Format: "A client with the name '%s' already exists." */
        public static final String DuplicateClientNameFormat = "A client with the name '%s' already exists.";
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

    public static class UserErrorMessages {
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

    public static class UserGroupErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid Group Id";
        public static final String GroupNameExists = "Group name exists in the system.";

        // Additional error messages
        public static final String ER001 = "One or more group ids is not valid.";
        public static final String ER002 = "User group name is required.";
        public static final String ER003 = "User group description is required";
        public static final String ER004 = "At least one user should be selected to include in the user group.";
    }

    public static class AddressErrorMessages {
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

    public static class TodoErrorMessages {
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
        public static final String ER001 = "This message cannot be edited as the email is scheduled within 10 minutes from the current time/";
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
        public static final String TitleTooLong = "Message title is too long (max 500 characters).";
        public static final String PublishDateRequiresSendAsEmail = "If publish date is set, sendAsEmail must be true.";
        public static final String CannotDisableSendAsEmailOnce = "Cannot disable sendAsEmail once it has been enabled.";
        public static final String CannotAddPublishDateAfterSent = "Cannot add publish date to a message that was already sent as email without scheduling.";
        public static final String CannotModifyScheduledPublishDate = "Cannot modify publish date for a scheduled email.";
        public static final String CannotDisableSendAsEmailScheduled = "Cannot disable sendAsEmail for a scheduled email.";
        public static final String InvalidUserId = "Invalid user ID.";
    }

    public static class WebTemplatesErrorMessages {
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
        /** Format: "Invalid Package Id. ID: %s" */
        public static final String InvalidIdWithIdFormat = "Invalid Package Id. ID: %s";
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
        /** Format: "Invalid column name: %s" */
        public static final String InvalidColumnNameFormat = "Invalid column name: %s";
        /** Format: "Invalid operator: %s" */
        public static final String InvalidOperatorFormat = "Invalid operator: %s";
        /**
         * Format: "Failed to retrieve ShipRocket pickup location ID after creation.
         * Response pickup_id: %d is invalid. Please verify the pickup location was
         * created successfully in ShipRocket."
         */
        public static final String ShipRocketPickupLocationIdInvalidFormat = "Failed to retrieve ShipRocket pickup location ID after creation. Response pickup_id: %d is invalid. Please verify the pickup location was created successfully in ShipRocket.";

        // Additional error messages
        public static final String ER001 = "Pickup location is required and should be valid.";
        public static final String ER002 = "Email on address is required and should be valid.";
        public static final String ER003 = "Phone on address is required and should be valid.";
        public static final String LocationNameTooLong = "Location name must be 36 characters or less (Shiprocket limit).";
    }

    public static class PromoErrorMessages {
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
        /** Format: "Product condition must be one of: %s" */
        public static final String InvalidConditionValueFormat = "Product condition must be one of: %s";
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
        /** Format: "HTTP %d when fetching image" */
        public static final String HttpErrorWhenFetchingImageFormat = "HTTP %d when fetching image";
        /** Format: "Invalid column name: %s" */
        public static final String InvalidColumnNameFormat = "Invalid column name: %s";
        /** Format: "Invalid operator: %s" */
        public static final String InvalidOperatorFormat = "Invalid operator: %s";
        /** Format: "Operator '%s' is not valid for %s column '%s'" */
        public static final String InvalidOperatorForColumnFormat = "Operator '%s' is not valid for %s column '%s'";
        public static final String ER013 = "Product not found with ID: %d";
        public static final String ER014 = "Product IDs list cannot be null or empty";
        public static final String NoPickupLocationsFound = "No pickup locations found for this product.";
        public static final String AtLeastOnePickupLocationRequired = "At least one pickup location with quantity must be provided.";
    }

    public static class LeadsErrorMessages {
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
        public static final String LEAD_DUPLICATE_EXTERNAL_ID = "Lead with the given external ID already exists.";
        public static final String LEAD_SOFT_DELETED_CONFLICT = "Lead is soft-deleted and cannot be modified.";
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
        public static final String MaxAttachmentsExceeded = "Maximum 30 attachments allowed per purchase order.";
        public static final String AtLeastOneProductRequired = "At least one product must be specified in products list";
        /** Format: "pricePerUnit is required for productId %s" */
        public static final String PricePerUnitRequiredForProductFormat = "pricePerUnit is required for productId %s";
        /**
         * Format: "pricePerUnit must be greater than or equal to 0 for productId %s"
         */
        public static final String PricePerUnitMustBeNonNegativeForProductFormat = "pricePerUnit must be greater than or equal to 0 for productId %s";
        /** Format: "Duplicate productId in products list: %s" */
        public static final String DuplicateProductIdFormat = "Duplicate productId in products list: %s";
    }

    public static class ProductReviewErrorMessages {
        // Standard error messages for product review order operations
        public static final String InvalidId = "Invalid product review id.";
        public static final String NotFound = "Product review not found.";
        /** Format: "Invalid column name: %s" */
        public static final String InvalidColumnNameFormat = "Invalid column name: %s";
        /** Format: "Invalid operator: %s" */
        public static final String InvalidOperatorFormat = "Invalid operator: %s";

        // additional error messages
        public static final String ER001 = "Product Review ratings should be present and should be between 0 and 5";
        public static final String ER002 = "Product Review text is required.";
        public static final String ER003 = "Product Review user id is required and should be valid.";
        public static final String ER004 = "Product Review product id is required and should be valid.";
        public static final String InvalidAuditUser = "Invalid user for audit fields.";
    }

    public static class TestExecutorErrorMessages {
        public static final String IoFailed = "Test execution I/O failed";
        public static final String Interrupted = "Test execution interrupted";
        public static final String ExecutionFailed = "Test execution failed";
        public static final String FailedToListSurefireReports = "Failed to list Surefire reports";
        /** Format: "Failed to parse Surefire report: %s" */
        public static final String FailedToParseSurefireReportFormat = "Failed to parse Surefire report: %s";
        /** Format: "I/O error during test execution: %s" */
        public static final String IoErrorDuringExecutionFormat = "I/O error during test execution: %s";
        /** Format: "Test execution interrupted: %s" */
        public static final String InterruptedFormat = "Test execution interrupted: %s";
        /** Format: "Test execution failed: %s" */
        public static final String ExecutionFailedFormat = "Test execution failed: %s";
        /** Format: "Tests failed. Exit code: %s" */
        public static final String TestsFailedExitCodeFormat = "Tests failed. Exit code: %s";
    }

    public static class QAErrorMessages {
        // Service discovery
        /** Format: "Service not found: %s. Available services: %s" */
        public static final String ServiceNotFoundFormat = "Service not found: %s. Available services: %s";
        /** Format: "Could not load service class: %s" */
        public static final String CouldNotLoadServiceClassFormat = "Could not load service class: %s";

        // Test run validation
        public static final String TestRunRequestCannotBeNull = "Test run request cannot be null";
        public static final String ServiceNameRequired = "Service name is required";
        public static final String AtLeastOneTestResultRequired = "At least one test result is required";

        // Test execution validation
        public static final String TestExecutionRequestCannotBeNull = "Test execution request cannot be null";
        public static final String TestClassNameRequired = "testClassName is required when running specific tests";
        public static final String MustSpecifyServiceNameOrTestClassName = "Must specify serviceName or testClassName when running tests by method name";
        /** Format: "No tests found for method: %s in class %s" */
        public static final String NoTestsFoundForMethodFormat = "No tests found for method: %s in class %s";
        public static final String MustSpecifyRunAllOrTestNamesOrMethod = "Must specify runAll, testNames, or methodName+testClassName";
        /** Format: "Test execution not found: %s" */
        public static final String TestExecutionNotFoundFormat = "Test execution not found: %s";
    }

    public static class ProductPickupLocationMappingErrorMessages {
        public static final String ProductIdRequired = "Product ID cannot be null.";
        public static final String PickupLocationIdRequired = "Pickup location ID cannot be null.";
        public static final String CreatedUserRequired = "Created user cannot be null or empty.";
        public static final String AtLeastOnePickupLocationRequired = "At least one pickup location with quantity must be provided.";
        public static final String AvailableStockMustBePositive = "Available stock for pickup location %d must be positive.";
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
        public static final String NotFound = "Shipment not found with ID: %d";
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
        public static final String AlreadyCancelled = "Shipment is already cancelled.";
        public static final String NoShipRocketOrderId = "Shipment does not have a ShipRocket order ID. Cannot cancel.";
        public static final String NoShipmentsFound = "No shipments found for this purchase order.";
        public static final String AccessDenied = "Access denied to this shipment.";

        // Courier validation errors
        public static final String CourierCompanyIdRequired = "Courier company ID is required.";
        public static final String CourierNameRequired = "Courier name is required.";
        public static final String CourierRateRequired = "Courier rate is required.";
        public static final String CourierMetadataRequired = "Courier metadata is required.";
        /** Format: "Invalid column name: %s" */
        public static final String InvalidColumnNameFormat = "Invalid column name: %s";
        /** Format: "Invalid operator: %s" */
        public static final String InvalidOperatorFormat = "Invalid operator: %s";
        /** Format: "Invalid shipment Id. Format error: %s" */
        public static final String InvalidIdFormatErrorFormat = "Invalid shipment Id. Format error: %s";
        /** Format: "Invalid shipment Id. %s" */
        public static final String InvalidIdWithMessageFormat = "Invalid shipment Id. %s";
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

    public static class ReturnShipmentErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid return shipment Id.";
        public static final String NotFound = "Return shipment not found with ID: %d";
        public static final String AlreadyCancelled = "Return shipment is already cancelled.";
        public static final String NoShipRocketOrderId = "Cannot cancel return shipment: ShipRocket return order ID not found.";
        public static final String ShipmentIdRequired = "Shipment ID is required.";
        public static final String AtLeastOneProductRequired = "At least one product must be selected for return.";
        public static final String ProductIdRequired = "Product ID is required for each return item.";
        public static final String ValidQuantityRequired = "Valid quantity is required for each return item.";
        public static final String ReturnReasonRequired = "Return reason is required for each product.";
        public static final String OnlyDeliveredCanReturn = "Can only create return for delivered shipments. Current status: %s";
        public static final String ProductNotInShipment = "Product ID %d is not part of this shipment.";
        public static final String ReturnQuantityExceeds = "Return quantity (%d) exceeds shipment quantity (%d) for product %d.";
        public static final String ProductPastReturnWindow = "Product '%s' is past its return window of %d days.";
        public static final String ProductNotReturnable = "Product '%s' is not returnable (return window is 0).";
        public static final String FailedToCreateReturn = "Failed to create return order in ShipRocket: %s";
        public static final String FailedToCancelReturn = "Failed to cancel return shipment in ShipRocket: %s";
    }

    public static class ShippingErrorMessages {
        // ShipRocket configuration errors
        public static final String ShipRocketCredentialsNotConfigured = "Shiprocket credentials not configured for this client.";
        public static final String DeliveryAddressNotFound = "Delivery address not found in order summary.";

        // Wallet errors
        public static final String WalletBalanceNotAvailable = "Wallet balance not available.";

        // Pickup location errors
        public static final String PickupLocationNameNotConfigured = "Pickup location name (addressNickName) is not configured for pickup location ID: %d";

        // Billing address validation errors
        public static final String BillingPostalCodeMustBeNumeric = "Billing postal code must be numeric. Provided value: %s";
        public static final String BillingPhoneMustBe10Digits = "Billing phone number must be exactly 10 digits. Provided value: %s";

        // Shipping address validation errors
        public static final String ShippingPostalCodeMustBeNumeric = "Shipping postal code must be numeric. Provided value: %s";
        public static final String ShippingPhoneMustBe10Digits = "Shipping phone number must be exactly 10 digits. Provided value: %s";

        // ShipRocket API errors
        public static final String ShipRocketApiNullResponse = "ShipRocket API returned null response for shipment ID: %d";
        public static final String ShipRocketOrderCreationFailed = "Failed to create ShipRocket order for shipment ID: %d. Error: %s";

        // ShipRocket operation errors
        public static final String AwbAssignmentFailed = "Failed to assign AWB code for ShipRocket shipment ID: %d. Error: %s";
        public static final String PickupGenerationFailed = "Failed to generate pickup for ShipRocket shipment ID: %d. Error: %s";
        public static final String ManifestGenerationFailed = "Failed to generate manifest for ShipRocket shipment ID: %d. Error: %s";
        public static final String LabelGenerationFailed = "Failed to generate shipping label for ShipRocket shipment ID: %d. Error: %s";
        public static final String InvoiceGenerationFailed = "Failed to generate invoice for ShipRocket shipment ID: %d. Error: %s";
        public static final String TrackingFetchFailed = "Failed to fetch tracking information for AWB code: %s. Error: %s";
        /** Format: "invalid status '%s'. Valid statuses are: %s" */
        public static final String InvalidShipRocketStatusFormat = "invalid status '%s'. Valid statuses are: %s";
        /**
         * Format: "Failed to serialize ShipRocket response to JSON for shipment ID %s:
         * %s"
         */
        public static final String FailedToSerializeShipRocketResponseFormat = "Failed to serialize ShipRocket response to JSON for shipment ID %s: %s";
    }

    public static class OrderOptimizationErrorMessages {
        public static final String NoProductsSpecified = "No products specified";
        public static final String DeliveryPostcodeRequired = "Delivery postcode is required";
        public static final String NoValidProductsFound = "No valid products found";
        public static final String NoValidAllocationStrategiesFound = "No valid allocation strategies found";
        public static final String NoShippingOptionsForAnyStrategy = "No shipping options available for any fulfillment strategy. This may be due to weight limits or route restrictions.";
        /** Format: "Optimization failed: %s" */
        public static final String OptimizationFailedFormat = "Optimization failed: %s";
        /** Format: "Product ID %s not found" */
        public static final String ProductNotFoundFormat = "Product ID %s not found";
        /**
         * Format: "Insufficient stock for product '%s'. Requested: %s, Available stock:
         * 0"
         */
        public static final String InsufficientStockZeroFormat = "Insufficient stock for product '%s'. Requested: %s, Available stock: 0";
        /**
         * Format: "Product '%s' cannot be packaged. Stock available: %s, but no
         * packages are configured at pickup locations. Requested: %s"
         */
        public static final String NoPackagesConfiguredFormat = "Product '%s' cannot be packaged. Stock available: %s, but no packages are configured at pickup locations. Requested: %s";
        /**
         * Format: "Product '%s' cannot be packaged. Stock available: %s, but no
         * packages are available at pickup locations (all packages have 0 quantity).
         * Requested: %s"
         */
        public static final String NoPackagesAvailableFormat = "Product '%s' cannot be packaged. Stock available: %s, but no packages are available at pickup locations (all packages have 0 quantity). Requested: %s";
        /**
         * Format: "Product '%s' cannot be packaged. Stock available: %s, but product
         * dimensions/weight exceed all available package limits. Requested: %s"
         */
        public static final String ProductExceedsPackageLimitsFormat = "Product '%s' cannot be packaged. Stock available: %s, but product dimensions/weight exceed all available package limits. Requested: %s";
        /**
         * Format: "Product '%s' cannot be packaged with available packages. Stock
         * available: %s, but %s. Requested: %s"
         */
        public static final String CannotPackageWithDetailFormat = "Product '%s' cannot be packaged with available packages. Stock available: %s, but %s. Requested: %s";
        public static final String NotEnoughPackagesForQuantity = "not enough packages available to pack the requested quantity";
        /**
         * Format: "Insufficient stock/packaging for product '%s'. Requested: %s,
         * Available stock: %s, Packable (considering packaging constraints): %s"
         */
        public static final String InsufficientStockPackagingFormat = "Insufficient stock/packaging for product '%s'. Requested: %s, Available stock: %s, Packable (considering packaging constraints): %s";
        /** Format: "Custom allocation validation failed:\n• %s" */
        public static final String CustomAllocationValidationFailedFormat = "Custom allocation validation failed:\n• %s";
        public static final String NoValidAllocationsSpecified = "No valid allocations specified";
    }

    public static class ShipmentProcessingErrorMessages {
        /** Format: "Product ID %s is not available at pickup location ID %s" */
        public static final String ProductNotAvailableAtPickupLocationFormat = "Product ID %s is not available at pickup location ID %s";
        /**
         * Format: "Insufficient stock for product ID %s at pickup location ID %s.
         * Available: %s, Requested: %s"
         */
        public static final String InsufficientProductStockFormat = "Insufficient stock for product ID %s at pickup location ID %s. Available: %s, Requested: %s";
        /** Format: "Package ID %s is not available at pickup location ID %s" */
        public static final String PackageNotAvailableAtPickupLocationFormat = "Package ID %s is not available at pickup location ID %s";
        /**
         * Format: "Insufficient packages for package ID %s at pickup location ID %s.
         * Available: %s, Requested: %s"
         */
        public static final String InsufficientPackageStockFormat = "Insufficient packages for package ID %s at pickup location ID %s. Available: %s, Requested: %s";
        /** Format: "%s %s" - for OPERATION_FAILED + payment message */
        public static final String OperationFailedWithMessageFormat = "%s %s";
    }

    public static class PaymentErrorMessages {
        // standard error messages
        public static final String InvalidId = "Invalid payment Id.";
        public static final String NotFound = "Payment not found.";
        public static final String AccessDenied = "Access denied to this payment.";
        public static final String CannotRefund = "This payment cannot be refunded.";

        // Configuration errors
        public static final String RazorpayApiKeyNotConfigured = "Razorpay API Key not configured for this client. Please configure in Client Settings.";
        public static final String RazorpayApiSecretNotConfigured = "Razorpay API Secret not configured for this client. Please configure in Client Settings.";

        // Status errors
        public static final String OnlyPendingApprovalCanBePaid = "Only orders with PENDING_APPROVAL status can be paid.";
        public static final String FollowUpPaymentStatusRequired = "Follow-up payments can only be made for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders.";

        // Validation errors
        public static final String PaymentDateRequired = "Payment date is required.";
        public static final String ValidPaymentAmountRequired = "Valid payment amount is required.";
        public static final String PaymentOrderNotFound = "Payment order not found. Please try again.";
    }

    public static class ConfigurationErrorMessages {
        // ImgBB
        public static final String ImgbbApiKeyNotConfigured = "ImgBB API key is not configured for this client.";
        /** Format: "Invalid imageLocation configuration: %s" */
        public static final String InvalidImageLocationConfigFormat = "Invalid imageLocation configuration: %s";

        // SendGrid
        public static final String SendGridEmailNotConfigured = "Email sender address is not configured in properties.";
        public static final String SendGridNameNotConfigured = "Email sender name is not configured in properties.";
        public static final String SendGridApiKeyNotConfigured = "SendGrid API key is not configured in properties.";
        public static final String BrevoApiKeyNotConfigured = "Brevo API key is not configured in properties.";

        // Client
        public static final String NoClientConfigurationFound = "No client configuration found.";
    }

    public static class CommonErrorMessages {
        // Pagination errors
        public static final String InvalidPagination = "Invalid pagination: end must be greater than start.";
        public static final String StartIndexCannotBeNegative = "Start index cannot be negative.";
        public static final String EndIndexMustBeGreaterThanZero = "End index must be greater than 0.";
        public static final String StartIndexMustBeLessThanEnd = "Start index must be less than end index.";

        // Logic operator errors
        public static final String InvalidLogicOperator = "Invalid logic operator. Must be 'AND' or 'OR'.";

        // Filter errors
        public static final String BooleanColumnsOnlySupportEquals = "Boolean columns only support 'equals' and 'notEquals' operators.";

        // List errors
        public static final String ListCannotBeNullOrEmpty = "%s list cannot be null or empty.";

        // Access errors
        public static final String AccessDeniedToPurchaseOrder = "Access denied to this purchase order.";

        // Confirmation errors
        public static final String FailedToSendConfirmationEmail = "Failed to send confirmation email.";

        // Permission errors
        public static final String AtLeastOnePermissionRequired = "At least one permission mapping is required for the user.";

        public static final String DATABASE_ERROR = "Database error";
        public static final String DATABASE_CONNECTION_ERROR = "Database connection error";
    }

    public static class OrderSummaryNotFoundMessage {
        public static final String NotFound = "Order summary not found for purchase order.";
    }

}
