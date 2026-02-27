package com.example.SpringApi;

public class ErrorMessages {
  // Common error types for controllers
  public static final String ERROR_BAD_REQUEST = "Bad Request";
  public static final String ERROR_NOT_FOUND = "Not Found";
  public static final String ERROR_INTERNAL_SERVER_ERROR = "Internal Server Error";
  public static final String ERROR_INVALID_CLIENT_ID = "Invalid client ID";
  public static final String ERROR_UNAUTHORIZED = "Unauthorized";

  public static final String INVALID_COLUMN =
      "Invalid column, the column should be one of the following: ";
  public static final String INVALID_ADDRESS =
      "Invalid address, please check all the address fields entered. Address line 1, address line 2, city, state, zip code are all required fields";
  public static final String INVALID_PHONE =
      "Invalid phone number, please check the entered phone number. Phone number should be of 10 numbers";
  public static final String UNAUTHORIZED = "You are unauthorized to do this action.";
  public static final String SERVER_ERROR = "An unexpected error occurred.";

  private ErrorMessages() {}

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
    private EventErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid event id provided.";

    // additional error messages
    public static final String ER001 = "Event cannot be null.";
    public static final String ER002 = "Event name is required.";
    public static final String ER003 =
        "Event description and event start/end times are required, and start time must not be after end time.";
    public static final String ER004 = "Time zone is required and must be a valid time zone.";
    public static final String ER005 = "Event location is required.";
  }

  public static class SupportErrorMessages {
    private SupportErrorMessages() {}

    // Additional Error messages
    public static final String ER001 =
        "The ticket has been created but there was an error uploading the attachments to the ticket, please try editing the ticket again in sometime.";
  }

  public static class CarrierErrorMessages {
    private CarrierErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid Carrier id provided.";

    // Additional error messages
    public static final String ER001 = "Given User and carrier are not mapped.";
    public static final String ER002 = "No Carriers found for given user.";
    public static final String ER003 = "There was an error fetching the issue types from jira.";
    public static final String ER004 =
        "There was an error fetching the carrier based on the wildcards.";
    public static final String ER005 = "Wilcard and api access key is required.";
    public static final String ER006 = "Invalid Credentials";
    public static final String ER007 = "Invalid Google Credentials Id";
  }

  public static class ClientErrorMessages {
    private ClientErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid Client Id.";
    public static final String INVALID_REQUEST = "Client request cannot be null.";

    /** Format: "A client with the name '%s' already exists." */
    public static final String DUPLICATE_CLIENT_NAME_FORMAT =
        "A client with the name '%s' already exists.";

    public static final String INVALID_NAME = "Client name is required and cannot be empty.";
    public static final String INVALID_DESCRIPTION =
        "Client description is required and cannot be empty.";
    public static final String INVALID_SUPPORT_EMAIL =
        "Client support email is required and cannot be empty.";
    public static final String INVALID_WEBSITE = "Client website is required and cannot be empty.";
    public static final String INVALID_SENDGRID_SENDER_NAME =
        "SendGrid sender name is required and cannot be empty.";
    public static final String INVALID_LOGO_UPLOAD = "Failed to upload logo to Firebase.";
    public static final String CLIENTS_LIST_NULL =
        "Cannot invoke \"java.util.List.iterator()\" because \"clients\" is null";
  }

  public static class LoginErrorMessages {
    private LoginErrorMessages() {}

    // standard error messages
    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String INVALID_ID = "Invalid User Id";
    public static final String INVALID_EMAIL = "Invalid User Email";
    public static final String ACCOUNT_CONFIRMED = "Account has already been confirmed";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String GOOGLE_USER_INCONSISTENCY =
        "User present in google users but not present in system users";
    public static final String ADD_USER = "There was an error creating the user in the database";
    public static final String LOGIN = "There was an error Logging in";

    // Additional error messages
    public static final String ER001 = "There was an error confirming the user email";
    public static final String ER002 = "Failed to Authenticate User";
    public static final String ER003 = "Cannot Reset a password for Oauth User";
    public static final String ER004 = "There was an error resting the user password";
    public static final String ER005 = "Please Confirm Your Account first";
    public static final String ER006 =
        "Your account has been locked please reset your password to login";
    public static final String ER007 =
        "Due to multiple failed attempts your account has been locked please reset your password to unlock your account";
    public static final String ER008 = "Email Exists in System, User has signed up using Oauth";
    public static final String ER009 = "Email Exists in System, User is a customer";
    public static final String ER010 = "Email Exists in System";
    public static final String ER011 =
        "You Currently do not have any permissions to use the System please contact your admin.";
    public static final String ER012 = "Email and password cannot be null or empty.";
    public static final String ER013 =
        "Login name, password, first name, last name, phone and date of birth are required in order to sign up.";
    public static final String ER014 = "User email is required in order to reset the password.";
    public static final String ER015 =
        "User email and api key is required to get the access token.";
    public static final String ER016 = "Please use Oauth to sign in";
    public static final String RESET_PASSWORD_EMAIL_FAILED = "Failed to send reset password email";
    public static final String NULL_REQUEST =
        "Cannot invoke \"com.example.SpringApi.Models.RequestModels.LoginRequestModel.getLoginName()\" because \"loginRequestModel\" is null";
  }

  public static class UserErrorMessages {
    private UserErrorMessages() {}

    // standard error messages
    public static final String UNAUTHORIZED =
        "Current user is not authorized to fetch/update details for the given user";
    public static final String INVALID_ID = "Invalid User Id";
    public static final String INVALID_EMAIL = "Invalid Email";
    public static final String INVALID_USER = "Invalid user provided for creation/modification.";
    public static final String INVALID_REQUEST = "User request cannot be null.";
    public static final String INVALID_LOGIN_NAME = "Login name is required and cannot be empty.";
    public static final String INVALID_FIRST_NAME = "First name is required and cannot be empty.";
    public static final String INVALID_LAST_NAME = "Last name is required and cannot be empty.";
    public static final String INVALID_PHONE = "Phone number is required and cannot be empty.";
    public static final String INVALID_ROLE = "Role is required and cannot be empty.";
    public static final String INVALID_DOB = "Date of birth is required.";
    public static final String ADD_USER = "There was an error adding the User";
    public static final String EDIT_USER = "There was an error editing the User";
    public static final String EMAIL_EXISTS = "The given email already exists in the system.";

    // Additional error messages
    public static final String ER001 =
        "There should be at least one row present in the imported excel sheet";
    public static final String ER002 = "Maximum 100 users can be imported at a time.";
    public static final String ER003 = "No permission set exists for the given user in the db.";
    public static final String ER004 = "Email is required and should be valid.";
    public static final String ER005 = "First name is required.";
    public static final String ER006 = "Last name is required.";
    public static final String ER007 =
        "User role is required and should be one of the following: .";
    public static final String ER008 = "Date of birth is required and should be valid.";
    public static final String ER009 = "Phone number is required and should be valid.";
    public static final String ER010 = "Failed to upload user profile picture.";
    public static final String ER011 = "Google credentials not found for client.";
  }

  public static class UserGroupErrorMessages {
    private UserGroupErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid Group Id";
    public static final String GROUP_NAME_EXISTS = "Group name exists in the system.";

    // Additional error messages
    public static final String ER001 = "One or more group ids is not valid.";
    public static final String ER002 = "User group name is required.";
    public static final String ER003 = "User group description is required";
    public static final String ER004 =
        "At least one user should be selected to include in the user group.";
  }

  public static class AddressErrorMessages {
    private AddressErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid Address Id.";
    public static final String NOT_FOUND = "Address not found for the give Id.";

    // Additional error messages
    public static final String ER001 = "Address line 1 is required.";
    public static final String ER002 = "City is required.";
    public static final String ER003 = "State is required.";
    public static final String ER004 = "Zip Code is required.";
    public static final String ER005 = "Country is required.";
    public static final String ER006 =
        "Invalid address type. Must be one of: HOME, WORK, BILLING, SHIPPING, OFFICE, WAREHOUSE.";
    public static final String ER007 = "Invalid postal code. Must be a 5 or 6 digit number.";
  }

  public static class TodoErrorMessages {
    private TodoErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid todo Id.";
    public static final String INVALID_REQUEST = "Todo request cannot be null.";
    public static final String INVALID_TASK = "Task is required and cannot be empty.";
    public static final String TASK_TOO_LONG = "Task cannot exceed 500 characters.";
  }

  public static class MessagesErrorMessages {
    private MessagesErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid message Id.";

    public static final String DB_ERROR = "DB Error";
    public static final String PAGE_ERROR = "Page error";
    public static final String USER_DB_ERROR = "User DB Error";
    public static final String LOOKUP_FAILED = "Lookup failed";
    public static final String NULL_PAGINATION_REQUEST =
        "Cannot invoke \"com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.getId()\" because \"paginationBaseRequestModel\" is null";

    // Additional error messages
    public static final String ER001 =
        "This message cannot be edited as the email is scheduled within 10 minutes from the current time/";
    public static final String ER002 = "There was an error cancelling the scheduled email.";
    public static final String ER003 = "Message title cannot be empty.";
    public static final String ER004 = "Message description cannot be empty.";
    public static final String ER005 =
        "Message publish date cannot be empty and needs to be greater than or equal to current date.";
    public static final String ER006 = "Message description markdown cannot be empty";
    public static final String ER007 = "Message description html cannot be empty";
    public static final String ER008 =
        "Atleast one user/usergroup needs to be present in the message.";
    public static final String ER009 =
        "Cannot schedule email in the past. Publish date must be in the future.";
    public static final String ER010 =
        "Cannot schedule email beyond 72 hours. SendGrid only allows scheduling within a 72-hour window from now (UTC timezone).";
    public static final String ER011 =
        "Cannot edit message. The scheduled email has already been sent (publish date has passed).";
    public static final String TITLE_TOO_LONG = "Message title is too long (max 500 characters).";
    public static final String PUBLISH_DATE_REQUIRES_SEND_AS_EMAIL =
        "If publish date is set, sendAsEmail must be true.";
    public static final String CANNOT_DISABLE_SEND_AS_EMAIL_ONCE =
        "Cannot disable sendAsEmail once it has been enabled.";
    public static final String CANNOT_ADD_PUBLISH_DATE_AFTER_SENT =
        "Cannot add publish date to a message that was already sent as email without scheduling.";
    public static final String CANNOT_MODIFY_SCHEDULED_PUBLISH_DATE =
        "Cannot modify publish date for a scheduled email.";
    public static final String CANNOT_DISABLE_SEND_AS_EMAIL_SCHEDULED =
        "Cannot disable sendAsEmail for a scheduled email.";
    public static final String INVALID_USER_ID = "Invalid user ID.";
    public static final String UPDATE_FAILED = "Update failed";
  }

  public static class WebTemplatesErrorMessages {
    private WebTemplatesErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid web template Id.";
    public static final String URL_EXISTS = "Same Url exists in the db, url should be unique.";

    // Additional error messages
    public static final String ER001 =
        "At least one sort option should be selected and should be one of the following: \"Price(low to high)\", \"Price(high to low)\", \"Rating\", \"Newest\", \"Oldest\"";
    public static final String ER002 =
        "At least one product id should be selected, and should be valid.";
    public static final String ER003 =
        "At least one filter option should be selected and should be one of the following: \"Price Range\", \"Category\", \"Brand\", \"Size\", \"Color\", \"Rating\", \"Availability\"";
    public static final String ER004 =
        "At least one filter option should be selected and should be one of the following: \"Credit Card\", \"Debit Card\", \"Amazon Pay\", \"Net Banking\", \"UPI\", \"EMI\", \"Gift Cards\", \"Cash on Delivery (COD)\"";
    public static final String ER005 = "Header color is required.";
    public static final String ER006 =
        "At least one Shipping State is required. A complete list of valid states list can be found here: ";
    public static final String ER007 =
        "At least one City mapping is required for each state. A complete list of valid state -> city mapping can be found here: ";
    public static final String ER008 =
        "Url should be present and valid. The url should also be a subdomain of ultimatecompany.com";
    public static final String ER009 =
        "Card header, card subtext, header - font styles are required. Each font style should have the font style, font color and the font size.";

    public static final String ER010 =
        "No items are present in the cart for the given userid and productid.";
    public static final String ER011 =
        "No items are present in the liked items for the given userid and productid.";
  }

  public static class PackageErrorMessages {
    private PackageErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid Package Id";

    /** Format: "Invalid Package Id. ID: %s" */
    public static final String INVALID_ID_WITH_ID_FORMAT = "Invalid Package Id. ID: %s";

    public static final String INVALID_REQUEST = "Package request cannot be null.";
    public static final String INVALID_PACKAGE_NAME =
        "Package name is required and cannot be empty.";
    public static final String INVALID_LENGTH =
        "Package length is required and must be greater than 0.";
    public static final String INVALID_BREADTH =
        "Package breadth is required and must be greater than 0.";
    public static final String INVALID_HEIGHT =
        "Package height is required and must be greater than 0.";
    public static final String INVALID_MAX_WEIGHT =
        "Package max weight is required and must be greater than or equal to 0.";
    public static final String INVALID_STANDARD_CAPACITY =
        "Package standard capacity is required and must be greater than 0.";
    public static final String INVALID_PRICE_PER_UNIT =
        "Package price per unit is required and must be greater than or equal to 0.";
    public static final String INVALID_PACKAGE_TYPE =
        "Package type is required and cannot be empty.";
    public static final String INVALID_CLIENT_ID = "Package client ID is required.";

    /** Format: "Invalid column name: %s" */
    public static final String INVALID_COLUMN_NAME_FORMAT = "Invalid column name: %s";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    /** Format: "Operator '%s' is not valid for column '%s'" */
    public static final String INVALID_OPERATOR_FOR_COLUMN_FORMAT =
        "Operator '%s' is not valid for column '%s'";

    /** Format: "Bulk item error: %s" */
    public static final String BULK_ITEM_ERROR_FORMAT = "Bulk item error: %s";

    /** Format: "Bulk critical error: %s" */
    public static final String BULK_CRITICAL_ERROR_FORMAT = "Bulk critical error: %s";

    public static final String UNKNOWN_PACKAGE_NAME = "unknown";
    public static final String BULK_IMPORT_KEY = "bulk_import";
    public static final String ENTITY_NAME = "Package";

    // Additional error messages
    public static final String ER001 =
        "Length, breadth and height are required and should be greater than 0";
    public static final String ER002 = "Quantity is required and should be greater than 0";
    public static final String ER003 =
        "Package with the same dimensions exists in the system, please update the quantity of the same package.";
  }

  public static class PickupLocationErrorMessages {
    private PickupLocationErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid pickup location Id.";
    public static final String NOT_FOUND = "Pickup location not found with ID: %s";
    public static final String DUPLICATE_NAME = "Duplicate pickup location name.";
    public static final String INVALID_REQUEST = "Pickup location request cannot be null.";
    public static final String INVALID_ADDRESS_NICK_NAME =
        "Address nickname is required and cannot be empty.";
    public static final String ADDRESS_NICK_NAME_TOO_LONG =
        "Address nickname cannot exceed 255 characters.";
    public static final String INVALID_SHIP_ROCKET_ID =
        "ShipRocket pickup location ID must be greater than 0.";

    /** Format: "Invalid column name: %s" */
    public static final String INVALID_COLUMN_NAME_FORMAT = "Invalid column name: %s";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    /**
     * Format: "Failed to retrieve ShipRocket pickup location ID after creation. Response pickup_id:
     * %d is invalid. Please verify the pickup location was created successfully in ShipRocket."
     */
    public static final String SHIP_ROCKET_PICKUP_LOCATION_ID_INVALID_FORMAT =
        "Failed to retrieve ShipRocket pickup location ID after creation. Response pickup_id: %d is invalid. Please verify the pickup location was created successfully in ShipRocket.";

    // Additional error messages
    public static final String ER001 = "Pickup location is required and should be valid.";
    public static final String ER002 = "Email on address is required and should be valid.";
    public static final String ER003 = "Phone on address is required and should be valid.";
    public static final String LOCATION_NAME_TOO_LONG =
        "Location name must be 36 characters or less (Shiprocket limit).";
  }

  public static class PromoErrorMessages {
    private PromoErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid promo Id.";
    public static final String INVALID_NAME = "Invalid promo code.";
    public static final String DUPLICATE_NAME = "Duplicate promo code name.";
    public static final String OVERLAPPING_PROMO_CODE =
        "A promo with this code already exists during the specified time period.";
    public static final String INVALID_REQUEST = "Promo request cannot be null.";
    public static final String INVALID_DESCRIPTION = "Description is required and cannot be empty.";
    public static final String DESCRIPTION_TOO_LONG = "Description cannot exceed 500 characters.";
    public static final String INVALID_DISCOUNT_VALUE =
        "Discount value is required and must be greater than or equal to 0.";
    public static final String INVALID_PERCENTAGE_VALUE = "Percentage discount cannot exceed 100%.";
    public static final String INVALID_PROMO_CODE = "Promo code is required and cannot be empty.";
    public static final String PROMO_CODE_TOO_LONG = "Promo code cannot exceed 100 characters.";
    public static final String INVALID_START_DATE = "Start date is required.";
    public static final String START_DATE_MUST_BE_TODAY_OR_FUTURE =
        "Start date must be today or in the future.";
    public static final String INVALID_EXPIRY_DATE = "Expiry date must be today or in the future.";
    public static final String EXPIRY_DATE_MUST_BE_AFTER_START_DATE =
        "Expiry date must be after or equal to start date.";
    public static final String PROMO_CODE_ALPHA_NUMERIC =
        "Promo code must contain only alphanumeric characters.";
    public static final String PROMO_CODE_LENGTH =
        "Promo code must be between 3 and 50 characters.";
    public static final String DISCOUNT_VALUE_GREATER_THAN_ZERO =
        "Discount value must be greater than zero.";
    public static final String DESCRIPTION_REQUIRED = "Description is required.";
    public static final String LONG_DESCRIPTION_TOO_LONG = "Description is too long.";
    public static final String CLIENT_ID_MISMATCH = "Client ID mismatch.";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    // Additional error messages
    public static final String ER001 = "Promo code is required.";
    public static final String ER002 = "Promo description is required.";
    public static final String ER003 =
        "Promo discount value is required and should be greater than 0.";
  }

  public static class ProductCategoryErrorMessages {
    private ProductCategoryErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid category Id.";
    public static final String INVALID_REQUEST = "Product category request cannot be null.";
    public static final String INVALID_NAME = "Category name is required and cannot be empty.";
    public static final String INVALID_IS_END = "Category isEnd flag is required.";
  }

  public static class ProductErrorMessages {
    private ProductErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid product Id.";
    public static final String INVALID_REQUEST = "Product request cannot be null.";
    public static final String INVALID_TITLE = "Product title is required and cannot be empty.";
    public static final String INVALID_DESCRIPTION =
        "Product description is required and cannot be empty.";
    public static final String INVALID_BRAND = "Product brand is required and cannot be empty.";
    public static final String INVALID_COLOR_LABEL =
        "Product color label is required and cannot be empty.";
    public static final String INVALID_CONDITION =
        "Product condition is required and cannot be empty.";

    /** Format: "Product condition must be one of: %s" */
    public static final String INVALID_CONDITION_VALUE_FORMAT =
        "Product condition must be one of: %s";

    public static final String INVALID_COUNTRY_OF_MANUFACTURE =
        "Product country of manufacture is required and cannot be empty.";
    public static final String INVALID_PRICE =
        "Product price is required and must be greater than or equal to 0.";
    public static final String INVALID_CATEGORY_ID = "Product category ID is required.";
    public static final String INVALID_CLIENT_ID = "Product client ID is required.";
    public static final String INVALID_PICKUP_LOCATION_ID =
        "Product pickup location ID is required and cannot be 0.";
    public static final String INVALID_LENGTH = "Product length must be greater than 0.";
    public static final String INVALID_BREADTH = "Product breadth must be greater than 0.";
    public static final String INVALID_HEIGHT = "Product height must be greater than 0.";
    public static final String INVALID_WEIGHT =
        "Product weight must be greater than or equal to 0.";
    public static final String INVALID_PICKUP_LOCATION_QUANTITY =
        "Pickup location quantity must be greater than or equal to 0.";

    /** Format: "Available stock for pickup location %d must be positive." */
    public static final String INVALID_PICKUP_LOCATION_QUANTITY_FORMAT =
        "Available stock for pickup location %d must be positive.";

    // additional error messages
    public static final String ER001 = "Product title is required.";
    public static final String ER002 = "Product Description plain text and html are required.";
    public static final String ER003 = "Product brand is required.";
    public static final String ER004 = "Product country of manufacture is required.";
    public static final String ER005 =
        "Product main, top, bottom, front, back, right, left and detail images are required and the urls should be valid.";
    public static final String ER006 =
        "ItemAvailable from date should not be null and should be greater than or equal to todays date.";
    public static final String ER007 = "Product category id should be present";
    public static final String ER008 = "Product category not found with ID: %s";
    public static final String ER009 = "Required image '%s' is missing";
    public static final String ER010 = "Failed to upload %s image to ImgBB";
    public static final String ER011 = "Failed to upload additional image %d to ImgBB";
    public static final String ER012 = "Failed to process image from URL: %s";

    /** Format: "HTTP %d when fetching image" */
    public static final String HTTP_ERROR_WHEN_FETCHING_IMAGE_FORMAT =
        "HTTP %d when fetching image";

    /** Format: "Invalid column name: %s" */
    public static final String INVALID_COLUMN_NAME_FORMAT = "Invalid column name: %s";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    /** Format: "Operator '%s' is not valid for %s column '%s'" */
    public static final String INVALID_OPERATOR_FOR_COLUMN_FORMAT =
        "Operator '%s' is not valid for %s column '%s'";

    public static final String ER013 = "Product not found with ID: %d";
    public static final String ER014 = "Product IDs list cannot be null or empty";
    public static final String NO_PICKUP_LOCATIONS_FOUND =
        "No pickup locations found for this product.";
    public static final String AT_LEAST_ONE_PICKUP_LOCATION_REQUIRED =
        "At least one pickup location with quantity must be provided.";
  }

  public static class LeadsErrorMessages {
    private LeadsErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid lead id.";

    /** Format: "Invalid column name: %s. Valid columns are: %s" */
    public static final String INVALID_COLUMN_NAME_WITH_VALID_COLUMNS_FORMAT =
        "Invalid column name: %s. Valid columns are: %s";

    /** Format: "Operator '%s' is not valid for column '%s'" */
    public static final String INVALID_OPERATOR_FOR_COLUMN_FORMAT =
        "Operator '%s' is not valid for column '%s'";

    /** Format: "Bulk item error: %s" */
    public static final String BULK_ITEM_ERROR_FORMAT = "Bulk item error: %s";

    /** Format: "Bulk critical error: %s" */
    public static final String BULK_CRITICAL_ERROR_FORMAT = "Bulk critical error: %s";

    // additional error messages
    public static final String ER001 = "Lead email is required.";
    public static final String ER002 = "Lead first name is required.";
    public static final String ER003 = "Lead last name is required.";
    public static final String ER004 = "Lead phone number is required.";
    public static final String ER005 = "Assigned Agent user id should be present in the database.";
    public static final String ER006 = "Website should be correctly formatted.";
    public static final String ER007 =
        "Invalid lead status, the lead status should be one of the following: ";
    public static final String ER008 = "Lead title is required.";
    public static final String ER009 = "Lead request cannot be null.";
    public static final String ER010 = "Invalid email format.";
    public static final String ER011 = "Invalid phone number format.";
    public static final String ER012 = "Client ID is required.";
    public static final String ER013 =
        "Address is required. Either provide an address object or an existing addressId.";
    public static final String ER014 = "Invalid address data provided.";
    public static final String ER015 = "Created by ID is required.";
    public static final String ER016 = "Company size must be greater than 0 if provided.";
    public static final String ER017 = "Assigned agent ID must be greater than 0 if provided.";
    public static final String ER018 = "Invalid user.";
    public static final String LEAD_DUPLICATE_EXTERNAL_ID =
        "Lead with the given external ID already exists.";
    public static final String LEAD_SOFT_DELETED_CONFLICT =
        "Lead is soft-deleted and cannot be modified.";
  }

  public static class PurchaseOrderErrorMessages {
    private PurchaseOrderErrorMessages() {}

    // Standard error messages for purchase order operations
    public static final String INVALID_ID = "Invalid purchase order Id.";
    public static final String INVALID_REQUEST = "Purchase order request cannot be null.";
    public static final String INVALID_VENDOR_NUMBER =
        "Vendor number is required and cannot be empty.";
    public static final String INVALID_TERMS_CONDITIONS =
        "Terms and conditions HTML is required and cannot be empty.";
    public static final String INVALID_ORDER_STATUS =
        "Order status is required and cannot be empty.";
    public static final String INVALID_ORDER_STATUS_VALUE = "Invalid purchase order status.";
    public static final String INVALID_PAYMENT_STATUS =
        "Payment status is required and cannot be empty.";
    public static final String INVALID_TOTAL_AMOUNT =
        "Total amount is required and must be greater than or equal to 0.";
    public static final String INVALID_AMOUNT_PAID =
        "Amount paid is required and must be greater than or equal to 0.";
    public static final String AMOUNT_PAID_EXCEEDS_TOTAL =
        "Amount paid cannot exceed total amount.";
    public static final String INVALID_CLIENT_ID = "Purchase order client ID is required.";
    public static final String INVALID_LEAD_ID = "Lead ID is required.";
    public static final String INVALID_ASSIGNED_LEAD_ID = "Assigned lead ID is required.";
    public static final String INVALID_ADDRESS_ID = "Purchase order address ID is required.";
    public static final String INVALID_PRIORITY =
        "Priority is required and must be one of: LOW, MEDIUM, HIGH, URGENT.";
    public static final String ALREADY_APPROVED = "Purchase order is already approved.";
    public static final String ALREADY_REJECTED = "Purchase order is already rejected.";
    public static final String ADDRESS_DATA_REQUIRED = "Address data is required in OrderSummary.";
    public static final String AT_LEAST_ONE_SHIPMENT_REQUIRED =
        "At least one shipment is required for purchase order.";
    public static final String IMGBB_API_KEY_NOT_CONFIGURED =
        "ImgBB API key is not configured for this client";
    public static final String INVALID_ATTACHMENT_DATA =
        "Each attachment must have a valid fileName (key) and base64 data (value)";
    public static final String FAILED_TO_UPLOAD_ATTACHMENTS = "Failed to upload attachments: %s";
    public static final String INVALID_COLUMN_NAME = "Invalid column name: %s";
    public static final String INVALID_OPERATOR = "Invalid operator: %s";
    public static final String BOOLEAN_COLUMNS_ONLY_SUPPORT_EQUALS =
        "Boolean columns only support 'equals' and 'notEquals' operators";
    public static final String COLUMNS_ONLY_SUPPORT_NUMERIC_OPERATORS =
        "%s columns only support numeric comparison operators";
    public static final String INVALID_PAGINATION =
        "Invalid pagination: end must be greater than start";

    // additional error messages
    public static final String ER001 =
        "Expected shipment date should be greater than or equal to the current date.";
    public static final String ER002 = "Assigned lead id should be present and cannot be 0";
    public static final String ER003 = "Terms and conditions html value is required.";
    public static final String ER004 = "There should be at least one product, quantity mapping.";
    public static final String ER005 =
        "Product id should be valid and present in the database and quantity for each product should be greater than 0";
    public static final String ER006 =
        "Purchase order has already been approved and cannot be approved again by the user";
    public static final String MAX_ATTACHMENTS_EXCEEDED =
        "Maximum 30 attachments allowed per purchase order.";
    public static final String AT_LEAST_ONE_PRODUCT_REQUIRED =
        "At least one product must be specified in products list";

    /** Format: "pricePerUnit is required for productId %s" */
    public static final String PRICE_PER_UNIT_REQUIRED_FOR_PRODUCT_FORMAT =
        "pricePerUnit is required for productId %s";

    /** Format: "pricePerUnit must be greater than or equal to 0 for productId %s" */
    public static final String PRICE_PER_UNIT_MUST_BE_NON_NEGATIVE_FOR_PRODUCT_FORMAT =
        "pricePerUnit must be greater than or equal to 0 for productId %s";

    /** Format: "Duplicate productId in products list: %s" */
    public static final String DUPLICATE_PRODUCT_ID_FORMAT =
        "Duplicate productId in products list: %s";
  }

  public static class ProductReviewErrorMessages {
    private ProductReviewErrorMessages() {}

    // Standard error messages for product review order operations
    public static final String INVALID_ID = "Invalid product review id.";
    public static final String NOT_FOUND = "Product review not found.";

    /** Format: "Invalid column name: %s" */
    public static final String INVALID_COLUMN_NAME_FORMAT = "Invalid column name: %s";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    // additional error messages
    public static final String ER001 =
        "Product Review ratings should be present and should be between 0 and 5";
    public static final String ER002 = "Product Review text is required.";
    public static final String ER003 = "Product Review user id is required and should be valid.";
    public static final String ER004 = "Product Review product id is required and should be valid.";
    public static final String INVALID_AUDIT_USER = "Invalid user for audit fields.";
  }

  public static class TestExecutorErrorMessages {
    private TestExecutorErrorMessages() {}

    public static final String IO_FAILED = "Test execution I/O failed";
    public static final String INTERRUPTED = "Test execution interrupted";
    public static final String EXECUTION_FAILED = "Test execution failed";
    public static final String FAILED_TO_LIST_SUREFIRE_REPORTS = "Failed to list Surefire reports";

    /** Format: "Failed to parse Surefire report: %s" */
    public static final String FAILED_TO_PARSE_SUREFIRE_REPORT_FORMAT =
        "Failed to parse Surefire report: %s";

    /** Format: "I/O error during test execution: %s" */
    public static final String IO_ERROR_DURING_EXECUTION_FORMAT =
        "I/O error during test execution: %s";

    /** Format: "Test execution interrupted: %s" */
    public static final String INTERRUPTED_FORMAT = "Test execution interrupted: %s";

    /** Format: "Test execution failed: %s" */
    public static final String EXECUTION_FAILED_FORMAT = "Test execution failed: %s";

    /** Format: "Tests failed. Exit code: %s" */
    public static final String TESTS_FAILED_EXIT_CODE_FORMAT = "Tests failed. Exit code: %s";
  }

  public static class QAErrorMessages {
    private QAErrorMessages() {}

    // Service discovery
    /** Format: "Service not found: %s. Available services: %s" */
    public static final String SERVICE_NOT_FOUND_FORMAT =
        "Service not found: %s. Available services: %s";

    /** Format: "Could not load service class: %s" */
    public static final String COULD_NOT_LOAD_SERVICE_CLASS_FORMAT =
        "Could not load service class: %s";

    // Test run validation
    public static final String TEST_RUN_REQUEST_CANNOT_BE_NULL = "Test run request cannot be null";
    public static final String SERVICE_NAME_REQUIRED = "Service name is required";
    public static final String AT_LEAST_ONE_TEST_RESULT_REQUIRED =
        "At least one test result is required";

    // Test execution validation
    public static final String TEST_EXECUTION_REQUEST_CANNOT_BE_NULL =
        "Test execution request cannot be null";
    public static final String SERVICE_NAME_NULL = "Service name cannot be null or empty";
    public static final String TEST_CLASS_NAME_REQUIRED =
        "testClassName is required when running specific tests";
    public static final String MUST_SPECIFY_SERVICE_NAME_OR_TEST_CLASS_NAME =
        "Must specify serviceName or testClassName when running tests by method name";

    /** Format: "No tests found for method: %s in class %s" */
    public static final String NO_TESTS_FOUND_FOR_METHOD_FORMAT =
        "No tests found for method: %s in class %s";

    public static final String MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD =
        "Must specify runAll, testNames, or methodName+testClassName";

    /** Format: "Test execution not found: %s" */
    public static final String TEST_EXECUTION_NOT_FOUND_FORMAT = "Test execution not found: %s";
  }

  public static class ProductPickupLocationMappingErrorMessages {
    private ProductPickupLocationMappingErrorMessages() {}

    public static final String PRODUCT_ID_REQUIRED = "Product ID cannot be null.";
    public static final String PICKUP_LOCATION_ID_REQUIRED = "Pickup location ID cannot be null.";
    public static final String CREATED_USER_REQUIRED = "Created user cannot be null or empty.";
    public static final String AT_LEAST_ONE_PICKUP_LOCATION_REQUIRED =
        "At least one pickup location with quantity must be provided.";
    public static final String AVAILABLE_STOCK_MUST_BE_POSITIVE =
        "Available stock for pickup location %d must be positive.";
  }

  public static class EmailErrorMessages {
    private EmailErrorMessages() {}

    // standard error messages
    public static final String INVALID_BATCH_ID = "Invalid Batch Id";

    // additional error messages
    public static final String ER001 = "Failed to send email";
    public static final String ER002 = "Failed to generate batch ID";
    public static final String ER003 = "Failed to cancel email";
  }

  public static class OrderSummaryErrorMessages {
    private OrderSummaryErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid order summary Id.";
    public static final String INVALID_REQUEST = "OrderSummary data is required.";
    public static final String ENTITY_TYPE_REQUIRED =
        "Entity type is required and cannot be empty.";
    public static final String INVALID_ENTITY_TYPE =
        "Invalid entity type. Must be 'PURCHASE_ORDER' or 'ORDER'.";
    public static final String ENTITY_ID_REQUIRED =
        "Entity ID is required and must be greater than 0.";
    public static final String PRODUCTS_SUBTOTAL_REQUIRED = "productsSubtotal is required.";
    public static final String PRODUCTS_SUBTOTAL_INVALID =
        "productsSubtotal must be greater than or equal to 0.";
    public static final String TOTAL_DISCOUNT_INVALID =
        "totalDiscount must be greater than or equal to 0.";
    public static final String PACKAGING_FEE_INVALID =
        "packagingFee must be greater than or equal to 0.";
    public static final String TOTAL_SHIPPING_INVALID =
        "totalShipping must be greater than or equal to 0.";
    public static final String INVALID_GST_PERCENTAGE = "GST percentage must be between 0 and 100.";
    public static final String ENTITY_ADDRESS_ID_REQUIRED =
        "Entity address ID is required and must be greater than 0.";
    public static final String PRIORITY_REQUIRED = "Priority is required and cannot be empty.";
    public static final String INVALID_PRIORITY =
        "Invalid priority. Must be 'LOW', 'MEDIUM', 'HIGH', or 'URGENT'.";
    public static final String PAID_AMOUNT_INVALID =
        "Paid amount must be greater than or equal to 0.";
    public static final String PAID_AMOUNT_EXCEEDS_GRAND_TOTAL =
        "Paid amount cannot exceed grand total.";
  }

  public static class ShipmentErrorMessages {
    private ShipmentErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid shipment Id.";
    public static final String NOT_FOUND = "Shipment not found with ID: %d";
    public static final String INVALID_REQUEST = "Shipment data is required.";
    public static final String ORDER_SUMMARY_ID_REQUIRED =
        "Order summary ID is required and must be greater than 0.";
    public static final String TOTAL_WEIGHT_REQUIRED = "Total weight is required.";
    public static final String TOTAL_WEIGHT_INVALID =
        "Total weight must be greater than or equal to 0.";
    public static final String TOTAL_QUANTITY_REQUIRED =
        "Total quantity is required and must be greater than 0.";
    public static final String PACKAGING_COST_REQUIRED = "packagingCost is required.";
    public static final String PACKAGING_COST_INVALID =
        "packagingCost must be greater than or equal to 0.";
    public static final String SHIPPING_COST_REQUIRED = "shippingCost is required.";
    public static final String SHIPPING_COST_INVALID =
        "shippingCost must be greater than or equal to 0.";
    public static final String COURIER_SELECTION_REQUIRED =
        "Each shipment must have at least one courier selected.";
    public static final String ALREADY_CANCELLED = "Shipment is already cancelled.";
    public static final String NO_SHIP_ROCKET_ORDER_ID =
        "Shipment does not have a ShipRocket order ID. Cannot cancel.";
    public static final String NO_SHIPMENTS_FOUND = "No shipments found for this purchase order.";
    public static final String ACCESS_DENIED = "Access denied to this shipment.";

    // Courier validation errors
    public static final String COURIER_COMPANY_ID_REQUIRED = "Courier company ID is required.";
    public static final String COURIER_NAME_REQUIRED = "Courier name is required.";
    public static final String COURIER_RATE_REQUIRED = "Courier rate is required.";
    public static final String COURIER_METADATA_REQUIRED = "Courier metadata is required.";

    /** Format: "Invalid column name: %s" */
    public static final String INVALID_COLUMN_NAME_FORMAT = "Invalid column name: %s";

    /** Format: "Invalid operator: %s" */
    public static final String INVALID_OPERATOR_FORMAT = "Invalid operator: %s";

    /** Format: "Invalid shipment Id. Format error: %s" */
    public static final String INVALID_ID_FORMAT_ERROR_FORMAT =
        "Invalid shipment Id. Format error: %s";

    /** Format: "Invalid shipment Id. %s" */
    public static final String INVALID_ID_WITH_MESSAGE_FORMAT = "Invalid shipment Id. %s";
  }

  public static class ShipmentProductErrorMessages {
    private ShipmentProductErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid shipment product Id.";
    public static final String INVALID_REQUEST = "Shipment product data is required.";
    public static final String SHIPMENT_ID_REQUIRED =
        "Shipment ID is required and must be greater than 0.";
    public static final String ALLOCATED_QUANTITY_REQUIRED =
        "Allocated quantity is required and must be greater than 0.";
    public static final String ALLOCATED_PRICE_REQUIRED = "Allocated price is required.";
    public static final String ALLOCATED_PRICE_INVALID =
        "Allocated price must be greater than or equal to 0.";
  }

  public static class ShipmentPackageErrorMessages {
    private ShipmentPackageErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid shipment package Id.";
    public static final String INVALID_REQUEST = "Shipment package data is required.";
    public static final String SHIPMENT_ID_REQUIRED =
        "Shipment ID is required and must be greater than 0.";
    public static final String QUANTITY_USED_REQUIRED =
        "Quantity used is required and must be greater than 0.";
    public static final String TOTAL_COST_REQUIRED = "Total cost is required.";
    public static final String TOTAL_COST_INVALID =
        "Total cost must be greater than or equal to 0.";
    public static final String AT_LEAST_ONE_PACKAGE_REQUIRED =
        "Each shipment must have at least one package.";
  }

  public static class ShipmentPackageProductErrorMessages {
    private ShipmentPackageProductErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid shipment package product Id.";
    public static final String INVALID_REQUEST = "Package product data is required.";
    public static final String SHIPMENT_PACKAGE_ID_REQUIRED =
        "Shipment package ID is required and must be greater than 0.";
    public static final String QUANTITY_REQUIRED =
        "Quantity is required and must be greater than 0.";
    public static final String AT_LEAST_ONE_PRODUCT_REQUIRED =
        "Each package must have at least one product.";
  }

  public static class ReturnShipmentErrorMessages {
    private ReturnShipmentErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid return shipment Id.";
    public static final String NOT_FOUND = "Return shipment not found with ID: %d";
    public static final String ALREADY_CANCELLED = "Return shipment is already cancelled.";
    public static final String NO_SHIP_ROCKET_ORDER_ID =
        "Cannot cancel return shipment: ShipRocket return order ID not found.";
    public static final String SHIPMENT_ID_REQUIRED = "Shipment ID is required.";
    public static final String AT_LEAST_ONE_PRODUCT_REQUIRED =
        "At least one product must be selected for return.";
    public static final String PRODUCT_ID_REQUIRED = "Product ID is required for each return item.";
    public static final String VALID_QUANTITY_REQUIRED =
        "Valid quantity is required for each return item.";
    public static final String RETURN_REASON_REQUIRED =
        "Return reason is required for each product.";
    public static final String ONLY_DELIVERED_CAN_RETURN =
        "Can only create return for delivered shipments. Current status: %s";
    public static final String PRODUCT_NOT_IN_SHIPMENT =
        "Product ID %d is not part of this shipment.";
    public static final String RETURN_QUANTITY_EXCEEDS =
        "Return quantity (%d) exceeds shipment quantity (%d) for product %d.";
    public static final String PRODUCT_PAST_RETURN_WINDOW =
        "Product '%s' is past its return window of %d days.";
    public static final String PRODUCT_NOT_RETURNABLE =
        "Product '%s' is not returnable (return window is 0).";
    public static final String FAILED_TO_CREATE_RETURN =
        "Failed to create return order in ShipRocket: %s";
    public static final String FAILED_TO_CANCEL_RETURN =
        "Failed to cancel return shipment in ShipRocket: %s";
  }

  public static class ShippingErrorMessages {
    private ShippingErrorMessages() {}

    public static final String NULL_SHIPPING_CALCULATION_REQUEST =
        "Shipping calculation request cannot be null.";

    // ShipRocket configuration errors
    public static final String SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED =
        "Shiprocket credentials not configured for this client.";
    public static final String DELIVERY_ADDRESS_NOT_FOUND =
        "Delivery address not found in order summary.";

    // Wallet errors
    public static final String WALLET_BALANCE_NOT_AVAILABLE = "Wallet balance not available.";

    // Pickup location errors
    public static final String PICKUP_LOCATION_NAME_NOT_CONFIGURED =
        "Pickup location name (addressNickName) is not configured for pickup location ID: %d";

    // Billing address validation errors
    public static final String BILLING_POSTAL_CODE_MUST_BE_NUMERIC =
        "Billing postal code must be numeric. Provided value: %s";
    public static final String BILLING_PHONE_MUST_BE10_DIGITS =
        "Billing phone number must be exactly 10 digits. Provided value: %s";

    // Shipping address validation errors
    public static final String SHIPPING_POSTAL_CODE_MUST_BE_NUMERIC =
        "Shipping postal code must be numeric. Provided value: %s";
    public static final String SHIPPING_PHONE_MUST_BE10_DIGITS =
        "Shipping phone number must be exactly 10 digits. Provided value: %s";

    // ShipRocket API errors
    public static final String SHIP_ROCKET_API_NULL_RESPONSE =
        "ShipRocket API returned null response for shipment ID: %d";
    public static final String SHIP_ROCKET_ORDER_CREATION_FAILED =
        "Failed to create ShipRocket order for shipment ID: %d. Error: %s";

    // ShipRocket operation errors
    public static final String AWB_ASSIGNMENT_FAILED =
        "Failed to assign AWB code for ShipRocket shipment ID: %d. Error: %s";
    public static final String PICKUP_GENERATION_FAILED =
        "Failed to generate pickup for ShipRocket shipment ID: %d. Error: %s";
    public static final String MANIFEST_GENERATION_FAILED =
        "Failed to generate manifest for ShipRocket shipment ID: %d. Error: %s";
    public static final String LABEL_GENERATION_FAILED =
        "Failed to generate shipping label for ShipRocket shipment ID: %d. Error: %s";
    public static final String INVOICE_GENERATION_FAILED =
        "Failed to generate invoice for ShipRocket shipment ID: %d. Error: %s";
    public static final String TRACKING_FETCH_FAILED =
        "Failed to fetch tracking information for AWB code: %s. Error: %s";

    /** Format: "invalid status '%s'. Valid statuses are: %s" */
    public static final String INVALID_SHIP_ROCKET_STATUS_FORMAT =
        "invalid status '%s'. Valid statuses are: %s";

    public static final String SHIP_ROCKET_ORDER_ID_MISSING = "order_id is missing from response";
    public static final String SHIP_ROCKET_SHIPMENT_ID_MISSING =
        "shipment_id is missing from response";
    public static final String SHIP_ROCKET_STATUS_MISSING = "status is missing from response";

    /** Format: "Failed to serialize ShipRocket response to JSON for shipment ID %s: %s" */
    public static final String FAILED_TO_SERIALIZE_SHIP_ROCKET_RESPONSE_FORMAT =
        "Failed to serialize ShipRocket response to JSON for shipment ID %s: %s";
  }

  public static class OrderOptimizationErrorMessages {
    private OrderOptimizationErrorMessages() {}

    public static final String NO_PRODUCTS_SPECIFIED = "No products specified";
    public static final String DELIVERY_POSTCODE_REQUIRED = "Delivery postcode is required";
    public static final String NO_VALID_PRODUCTS_FOUND = "No valid products found";
    public static final String NO_VALID_ALLOCATION_STRATEGIES_FOUND =
        "No valid allocation strategies found";
    public static final String NO_SHIPPING_OPTIONS_FOR_ANY_STRATEGY =
        "No shipping options available for any fulfillment strategy. This may be due to weight limits or route restrictions.";

    /** Format: "Optimization failed: %s" */
    public static final String OPTIMIZATION_FAILED_FORMAT = "Optimization failed: %s";

    /** Format: "Product ID %s not found" */
    public static final String PRODUCT_NOT_FOUND_FORMAT = "Product ID %s not found";

    /** Format: "Insufficient stock for product '%s'. Requested: %s, Available stock: 0" */
    public static final String INSUFFICIENT_STOCK_ZERO_FORMAT =
        "Insufficient stock for product '%s'. Requested: %s, Available stock: 0";

    /**
     * Format: "Product '%s' cannot be packaged. Stock available: %s, but no packages are configured
     * at pickup locations. Requested: %s"
     */
    public static final String NO_PACKAGES_CONFIGURED_FORMAT =
        "Product '%s' cannot be packaged. Stock available: %s, but no packages are configured at pickup locations. Requested: %s";

    /**
     * Format: "Product '%s' cannot be packaged. Stock available: %s, but no packages are available
     * at pickup locations (all packages have 0 quantity). Requested: %s"
     */
    public static final String NO_PACKAGES_AVAILABLE_FORMAT =
        "Product '%s' cannot be packaged. Stock available: %s, but no packages are available at pickup locations (all packages have 0 quantity). Requested: %s";

    /**
     * Format: "Product '%s' cannot be packaged. Stock available: %s, but product dimensions/weight
     * exceed all available package limits. Requested: %s"
     */
    public static final String PRODUCT_EXCEEDS_PACKAGE_LIMITS_FORMAT =
        "Product '%s' cannot be packaged. Stock available: %s, but product dimensions/weight exceed all available package limits. Requested: %s";

    /**
     * Format: "Product '%s' cannot be packaged with available packages. Stock available: %s, but
     * %s. Requested: %s"
     */
    public static final String CANNOT_PACKAGE_WITH_DETAIL_FORMAT =
        "Product '%s' cannot be packaged with available packages. Stock available: %s, but %s. Requested: %s";

    public static final String NOT_ENOUGH_PACKAGES_FOR_QUANTITY =
        "not enough packages available to pack the requested quantity";

    /**
     * Format: "Insufficient stock/packaging for product '%s'. Requested: %s, Available stock: %s,
     * Packable (considering packaging constraints): %s"
     */
    public static final String INSUFFICIENT_STOCK_PACKAGING_FORMAT =
        "Insufficient stock/packaging for product '%s'. Requested: %s, Available stock: %s, Packable (considering packaging constraints): %s";

    /** Format: "Custom allocation validation failed:\n• %s" */
    public static final String CUSTOM_ALLOCATION_VALIDATION_FAILED_FORMAT =
        "Custom allocation validation failed:\n• %s";

    public static final String NO_VALID_ALLOCATIONS_SPECIFIED = "No valid allocations specified";
  }

  public static class ShipmentProcessingErrorMessages {
    private ShipmentProcessingErrorMessages() {}

    /** Format: "Product ID %s is not available at pickup location ID %s" */
    public static final String PRODUCT_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT =
        "Product ID %s is not available at pickup location ID %s";

    /**
     * Format: "Insufficient stock for product ID %s at pickup location ID %s. Available: %s,
     * Requested: %s"
     */
    public static final String INSUFFICIENT_PRODUCT_STOCK_FORMAT =
        "Insufficient stock for product ID %s at pickup location ID %s. Available: %s, Requested: %s";

    /** Format: "Package ID %s is not available at pickup location ID %s" */
    public static final String PACKAGE_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT =
        "Package ID %s is not available at pickup location ID %s";

    /**
     * Format: "Insufficient packages for package ID %s at pickup location ID %s. Available: %s,
     * Requested: %s"
     */
    public static final String INSUFFICIENT_PACKAGE_STOCK_FORMAT =
        "Insufficient packages for package ID %s at pickup location ID %s. Available: %s, Requested: %s";

    /** Format: "%s %s" - for OPERATION_FAILED + payment message */
    public static final String OPERATION_FAILED_WITH_MESSAGE_FORMAT = "%s %s";
  }

  public static class PaymentErrorMessages {
    private PaymentErrorMessages() {}

    // standard error messages
    public static final String INVALID_ID = "Invalid payment Id.";
    public static final String NOT_FOUND = "Payment not found.";
    public static final String ACCESS_DENIED = "Access denied to this payment.";
    public static final String CANNOT_REFUND = "This payment cannot be refunded.";

    // Configuration errors
    public static final String RAZORPAY_API_KEY_NOT_CONFIGURED =
        "Razorpay API Key not configured for this client. Please configure in Client Settings.";
    public static final String RAZORPAY_API_SECRET_NOT_CONFIGURED =
        "Razorpay API Secret not configured for this client. Please configure in Client Settings.";

    // Status errors
    public static final String ONLY_PENDING_APPROVAL_CAN_BE_PAID =
        "Only orders with PENDING_APPROVAL status can be paid.";
    public static final String FOLLOW_UP_PAYMENT_STATUS_REQUIRED =
        "Follow-up payments can only be made for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders.";

    // Validation errors
    public static final String PAYMENT_DATE_REQUIRED = "Payment date is required.";
    public static final String VALID_PAYMENT_AMOUNT_REQUIRED = "Valid payment amount is required.";
    public static final String PAYMENT_ORDER_NOT_FOUND =
        "Payment order not found. Please try again.";

    // Additional format messages referenced by PaymentService
    public static final String FAILED_TO_CREATE_RAZORPAY_ORDER_FORMAT =
        "Failed to create Razorpay order: %s";
    public static final String PAYMENT_AMOUNT_EXCEEDS_PENDING_AMOUNT_FORMAT =
        "Payment amount %s exceeds pending amount %s";
    public static final String REFUND_AMOUNT_EXCEEDS_REFUNDABLE_AMOUNT_FORMAT =
        "Refund amount exceeds refundable amount: %s";
    public static final String FAILED_TO_PROCESS_REFUND_FORMAT = "Failed to process refund: %s";
    public static final String PAYMENT_AMOUNT_EXCEEDS_GRAND_TOTAL_FORMAT =
        "Total paid %s exceeds grand total %s";
  }

  public static class ConfigurationErrorMessages {
    private ConfigurationErrorMessages() {}

    // ImgBB
    public static final String IMGBB_API_KEY_NOT_CONFIGURED =
        "ImgBB API key is not configured for this client.";

    /** Format: "Invalid imageLocation configuration: %s" */
    public static final String INVALID_IMAGE_LOCATION_CONFIG_FORMAT =
        "Invalid imageLocation configuration: %s";

    // SendGrid
    public static final String SEND_GRID_EMAIL_NOT_CONFIGURED =
        "Email sender address is not configured in properties.";
    public static final String SEND_GRID_NAME_NOT_CONFIGURED =
        "Email sender name is not configured in properties.";
    public static final String SEND_GRID_API_KEY_NOT_CONFIGURED =
        "SendGrid API key is not configured in properties.";
    public static final String BREVO_API_KEY_NOT_CONFIGURED =
        "Brevo API key is not configured in properties.";

    // Client
    public static final String NO_CLIENT_CONFIGURATION_FOUND = "No client configuration found.";
  }

  public static class CommonErrorMessages {
    private CommonErrorMessages() {}

    // Pagination errors
    public static final String INVALID_PAGINATION =
        "Invalid pagination: end must be greater than start.";
    public static final String START_INDEX_CANNOT_BE_NEGATIVE = "Start index cannot be negative.";
    public static final String END_INDEX_MUST_BE_GREATER_THAN_ZERO =
        "End index must be greater than 0.";
    public static final String START_INDEX_MUST_BE_LESS_THAN_END =
        "Start index must be less than end index.";

    // Logic operator errors
    public static final String INVALID_LOGIC_OPERATOR =
        "Invalid logic operator. Must be 'AND' or 'OR'.";

    // Filter errors
    public static final String INVALID_COLUMN_NAME = "Invalid column name: %s";
    public static final String BOOLEAN_COLUMNS_ONLY_SUPPORT_EQUALS =
        "Boolean columns only support 'equals' and 'notEquals' operators.";

    // List errors
    public static final String LIST_CANNOT_BE_NULL_OR_EMPTY = "%s list cannot be null or empty.";

    // Access errors
    public static final String ACCESS_DENIED_TO_PURCHASE_ORDER =
        "Access denied to this purchase order.";

    // Confirmation errors
    public static final String FAILED_TO_SEND_CONFIRMATION_EMAIL =
        "Failed to send confirmation email.";

    // Permission errors
    public static final String AT_LEAST_ONE_PERMISSION_REQUIRED =
        "At least one permission mapping is required for the user.";

    public static final String DATABASE_ERROR = "Database error";
    public static final String DATABASE_CONNECTION_ERROR = "Database connection error";
    public static final String INVALID_BULK_REQUEST =
        "Invalid bulk request, the list cannot be null or empty.";
    public static final String EMPTY_LIST = "List cannot be empty.";
    public static final String NULL_LIST = "List cannot be null.";
    public static final String CONTEXT_MISSING = "Context values are missing.";
    public static final String CRITICAL_FAILURE = "Critical failure.";
  }

  public static class OrderSummaryNotFoundMessage {
    private OrderSummaryNotFoundMessage() {}

    public static final String NOT_FOUND = "Order summary not found for purchase order.";
    public static final String PURCHASE_ORDER_NOT_FOUND =
        "Order summary not found for purchase order.";
  }
}

