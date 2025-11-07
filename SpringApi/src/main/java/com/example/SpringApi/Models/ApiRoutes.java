package com.example.SpringApi.Models;

public class ApiRoutes {
    public static class ApiControllerNames
    {
        public static final String ADDRESS = "Address";
        public static final String CARRIER = "Carrier";
        public static final String CLIENT = "Client";
        public static final String USER_GROUP = "UserGroup";
        public static final String MESSAGE = "Message";
        public static final String PACKAGE = "Package";
        public static final String ORDER = "Order";
        public static final String PAYMENTS = "Payments";
        public static final String PICKUP_LOCATION = "PickupLocation";
        public static final String PRODUCT_CATEGORY = "ProductCategory";
        public static final String PRODUCT = "Product";
        public static final String PRODUCT_REVIEW = "ProductReview";
        public static final String PROMO = "Promo";
        public static final String USER_CART = "UserCart";
        public static final String USER = "User";
        public static final String BULK = "Bulk";
        public static final String GRID = "Grid";
        public static final String USERLOG = "UserLog";
        public static final String LOGIN = "Login";
        public static final String EVENT = "Event";
        public static final String PERMISSION = "Permission";
        public static final String TODO = "Todo";
        public static final String SUPPORT = "Support";
        public static final String LEAD = "Lead";
        public static final String PURCHASE_ORDER = "PurchaseOrder";
        public static final String SALES_ORDER = "SalesOrder";
        public static final String WEB_TEMPLATE = "WebTemplate";
        public static final String DATA = "Data";

    }
    public static class EventSubRoute {
        public static final String GET_ALL_EVENTS_FOR_USERID_BASED_ON_MONTH = "getAllEventsForUserIdBasedOnMonth";
        public static final String CREATE_EVENT = "createEvent";
        public static final String UPDATE_EVENT = "updateEvent";
        public static final String TOGGLE_EVENT = "toggleEvent";
        public static final String GET_EVENT_DETAILS_BY_ID = "getEventDetailsById";
    }

    public static class SupportSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";
        public static final String GET_ATTACHMENT_BY_ID = "getAttachmentById";

        // Api + frontend Endpoints
        public static final String GET_SUPPORT_TICKETS_IN_BATCHES = "getSupportTicketsInBatches";
        public static final String CREATE_TICKET = "createTicket";
        public static final String EDIT_TICKET = "editTicket";
        public static final String DELETE_TICKET = "deleteTicket";
        public static final String ADD_COMMENT = "addComment";
        public static final String EDIT_COMMENT = "editComment";
        public static final String DELETE_COMMENT = "deleteComment";
        public static final String GET_COMMENTS_FROM_TICKET = "getCommentsFromTicket";
        public static final String GET_TICKET_DETAILS_BY_ID = "getTicketDetailsById";
        public static final String GET_ATTACHMENT_FROM_TICKET = "getAttachmentFromTicket";
    }

    public static class AddressSubRoute {
        public static final String TOGGLE_ADDRESS = "toggleAddress";
        public static final String GET_ADDRESS_BY_USER_ID = "getAddressByUserId";
        public static final String GET_ADDRESS_BY_CLIENT_ID = "getAddressByClientId";
        public static final String INSERT_ADDRESS = "insertAddress";
        public static final String UPDATE_ADDRESS = "updateAddress";
        public static final String GET_ADDRESS_BY_ID = "getAddressById";
    }

    public static class ClientSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String CREATE_CLIENT = "createClient";
        public static final String UPDATE_CLIENT = "updateClient";
        public static final String TOGGLE_CLIENT = "toggleClient";
        public static final String GET_CLIENT_BY_ID = "getClientById";
        public static final String GET_CLIENTS = "getClients";
        public static final String GET_CLIENTS_BY_USER = "getClientsByUser";
    }

    public static class UserSubRoute {
        public static final String TOGGLE_USER = "toggleUser";
        public static final String GET_USER_BY_ID = "getUserById";
        public static final String GET_USER_BY_EMAIL = "getUserByEmail";
        public static final String CREATE_USER = "createUser";
        public static final String UPDATE_USER = "updateUser";
        public static final String CONFIRM_EMAIL = "confirmEmail";
    }


    public static class CarriersSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";
        public static final String SET_CARRIER = "setCarrier";
        public static final String GET_LOGGED_IN_CARRIER = "getLoggedInCarrier";
        public static final String GET_CARRIER_IMAGE = "getCarrierImage";

        // Api + frontend Endpoints
        public static final String GET_CARRIER_DETAILS_BY_ID = "getCarrierDetailsById";
        public static final String IS_USER_MAPPED_TO_CARRIER = "isUserMappedToCarrier";
        public static final String GET_CARRIER_IN_BATCHES = "getCarrierInBatches";
        public static final String GET_API_KEYS = "getApiKeys";
        public static final String UPDATE_API_KEYS = "updateApiKeys";

        // Public endpoints
        public static final String GET_CARRIER_ID_BY_WEB_TEMPLATE_WILDCARD = "getCarrierIdByWebTemplateWildCard";
        public static final String GET_TOKEN_FOR_WEBTEMPLATE = "getTokenForWebTemplate";
    }

    public static class PackageSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String GET_PACKAGES_IN_BATCHES = "getPackagesInBatches";
        public static final String CREATE_PACKAGE = "createPackage";
        public static final String UPDATE_PACKAGE = "updatePackage";
        public static final String TOGGLE_PACKAGE = "togglePackage";
        public static final String GET_PACKAGE_BY_ID = "getPackageById";
        public static final String GET_ALL_PACKAGES_IN_SYSTEM = "getAllPackagesInSystem";
        public static final String GET_PACKAGES_BY_PICKUP_LOCATION_ID = "getPackagesByPickupLocationId";
    }

    public static class UserGroupSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String GET_USER_GROUPS_IN_BATCHES = "getUserGroupsInBatches";
        public static final String GET_USER_GROUP_DETAILS_BY_ID = "getUserGroupDetailsById";
        public static final String CREATE_USER_GROUP = "createUserGroup";
        public static final String UPDATE_USER_GROUP = "updateUserGroup";
        public static final String GET_USER_IDS_IN_USER_GROUP = "getUserIdsInUserGroup";
        public static final String TOGGLE_USER_GROUP = "toggleUserGroup";
        public static final String GET_USER_GROUP_IDS_BY_USERID = "getUserGroupIdsByUserId";

    }

    public static class LoginSubRoute {
        // Purely frontend endpoints
        public static final String CHECK_IF_USER_IS_LOGGED_IN = "checkIfUserIsLoggedIn";
        public static final String LOG_OUT = "logOut";

        // Api + frontend Endpoints
        public static final String CONFIRM_EMAIL = "confirmEmail";
        public static final String SIGN_IN = "signIn";
        public static final String GOOGLE_SIGN_IN = "googleSignIn";
        public static final String RESET_PASSWORD = "resetPassword";
        public static final String GET_TOKEN = "getToken";
    }

    public static class MessagesSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String GET_MESSAGES_IN_BATCHES = "getMessagesInBatches";
        public static final String CREATE_MESSAGE = "createMessage";
        public static final String UPDATE_MESSAGE = "updateMessage";
        public static final String TOGGLE_MESSAGE = "toggleMessage";
        public static final String GET_MESSAGE_DETAILS_BY_ID = "getMessageDetailsById";
        public static final String GET_USERS_IN_MESSAGE = "getUsersInMessage";
        public static final String GET_USER_GROUPS_IN_MESSAGE = "getUserGroupsInMessage";
        public static final String GET_MESSAGES_BY_USER_ID = "getMessagesByUserId";
        public static final String SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID = "setMessageReadByUserIdAndMessageId";
    }

    public enum OrdersSubRoute
    {
        GetOrdersInBatches,
        PlaceOrder,
        GetOrderInBatches,
        GetPricingInformationByQuantityandIds,
        FilterOrders,
        GetOrderDetailsById,
        UpdateShippingDetails,
        RemoveProductsInOrder,
        OnPaymentSuccess,
        UpdatePaymentInfoModelData,
        UpdateProductQuantityInOrder,
        CancelOrder,
        GetCancellationPricing,
        GetOrderStatistics,
        GetOrderDetailsById_Pb,
        OnPaymentSuccess_Pb
    }

    public static class PaymentsSubRoute {
        public static final String INSERT_PAYMENT = "insertPayment";
        public static final String GET_PAYMENTS_IN_BATCHES = "getPaymentsInBatches";
        public static final String GET_REFUNDS_IN_BATCHES = "getRefundsInBatches";
        public static final String GET_PAYMENT_STATISTICS = "getPaymentStatistics";
        public static final String PROCESS_REFUND = "processRefund";
        public static final String GET_REFUNDS_QUEUE = "getRefundsQueue";
    }


    public static class PickupLocationsSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String CREATE_PICKUP_LOCATION = "createPickupLocation";
        public static final String GET_PICKUP_LOCATIONS_IN_BATCHES = "getPickupLocationsInBatches";
        public static final String GET_ALL_PICKUP_LOCATIONS = "getAllPickupLocations";
        public static final String UPDATE_PICKUP_LOCATION = "updatePickupLocation";
        public static final String TOGGLE_PICKUP_LOCATION = "togglePickupLocation";
        public static final String GET_PICKUP_LOCATION_BY_ID = "getPickupLocationById";
    }

    public static class ProductCategorySubRoute
    {
        public static final String GET_ROOT_CATEGORIES = "getRootCategories";
        public static final String GET_CHILD_CATEGORIES_GIVEN_PARENT_ID = "getChildCategoriesGivenParentId";
        public static final String GET_CATEGORY_BY_NAME = "getCategoryByName";
        public static final String FIND_CATEGORIES_WITHOUT_CHILDREN = "findCategoriesWithoutChildren";
    }

    public static class ProductsSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";
        public static final String GET_PRODUCT_CATEGORIES = "getProductCategories";
        public static final String GET_STATIC_IMAGE = "getStaticImage";
        public static final String GET_PRODUCT_IMAGE = "getProductImage";

        // Api + frontend Endpoints
        public static final String ADD_PRODUCT = "addProduct";
        public static final String EDIT_PRODUCT = "editProduct";
        public static final String TOGGLE_DELETE_PRODUCT = "toggleDeleteProduct";
        public static final String TOGGLE_RETURN_PRODUCT = "toggleReturnProduct";
        public static final String GET_PRODUCT_DETAILS_BY_ID = "getProductDetailsById";
        public static final String GET_PRODUCT_DETAILS_BY_IDS = "getProductDetailsByIds";
        public static final String GET_PRODUCTS_IN_BATCHES = "getProductsInBatches";

        // Public endpoints
        public static final String ADD_ITEM_TO_CART = "addItemToCart";
        public static final String ADD_ITEM_TO_LIKED_ITEMS = "addItemToLikedItems";
        public static final String REMOVE_ITEM_FROM_CART = "removeItemFromCart";
        public static final String REMOVE_ITEM_FROM_LIKED_ITEMS = "removeItemFromLikedItems";
        public static final String GET_PRODUCTS_IN_BATCHES_PUBLIC = "getProductsInBatches_Public";
        public static final String GET_PRODUCT_DETAILS_BY_IDS_PUBLIC = "getProductDetailsByIds_Public";
        public static final String GET_PRODUCT_DETAILS_BY_ID_PUBLIC = "getProductDetailsById_Public";

    }

    public static class PromosSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String CREATE_PROMO = "createPromo";
        public static final String GET_PROMOS_IN_BATCHES = "getPromosInBatches";
        public static final String GET_PROMO_DETAILS_BY_ID = "getPromoDetailsById";
        public static final String TOGGLE_PROMO = "togglePromo";
        public static final String GET_PROMO_DETAILS_BY_NAME = "getPromoDetailsByName";
    }

    public static class UserLogSubRoute {
        public static final String GET_USER_LOGS_IN_BATCHES_BY_USERID = "getUserLogsInBatchesByUserId";
    }

    public static class GridSubRoute {
        public static final String UPDATE_GRID_VISIBILITY_PREFERENCE = "updateGridVisibilityPreference";
        public static final String UPDATE_GRID_DENSITY_PREFERENCE = "updateGridDensityVisibilityPreference";
        public static final String UPDATE_ROWS_PER_PAGE_PREFERENCE = "updateRowsPerPagePreference";
        public static final String GET_GRID_VISIBILITY_PREFERENCE = "getGridVisibilityPreference";
    }

    public static class UsersSubRoute {
        // purely frontend endpoints
        public static final String GET_LOGGED_IN_USER_PERMISSIONS = "getLoggedInUserPermissions";
        public static final String GET_LOGGED_IN_USER = "getLoggedInUser";
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";
        public static final String GET_PROFILE_IMAGE = "getProfileImage";
        public static final String BULK_CREATE_USER = "bulkCreateUser";

        // Api + frontend Endpoints
        public static final String GET_USER_PERMISSIONS_BY_ID = "getUserPermissionsById";

        public static final String IS_EMAIL_AVAILABLE_IN_SYSTEM = "isEmailAvailableInSystem";
        public static final String CREATE_USER = "createUser";
        public static final String TOGGLE_USER = "toggleUser";
        public static final String UPDATE_USER = "updateUser";
        public static final String FETCH_ALL_USERS_IN_SYSTEM = "fetchAllUsersInSystem";
        public static final String GET_USER_BY_EMAIL = "getUserByEmail";
        public static final String GET_USER_BY_ID = "getUserById";
        public static final String GOOGLE_SIGN_IN = "googleSignIn";
        public static final String GET_USERS_IN_CARRIER_IN_BATCHES = "getUsersInCarrierInBatches";
        public static final String GET_USER_BY_EMAIL_PB = "getUserByEmail_Pb";
    }

    public static class PermissionsSubRoute {

        public static final String CREATE_PERMISSIONS = "CreatePermissions";
        public static final String UPDATE_PERMISSIONS = "UpdatePermissions";
        public static final String FETCH_PERMISSION_BY_ID = "FetchPermissionById";
    }


    public static class TodoSubRoute {
        public static final String ADD_ITEM = "addItem";
        public static final String UPDATE_ITEM = "updateItem";
        public static final String DELETE_ITEM = "deleteItem";
        public static final String TOGGLE_DONE = "toggleDone";
        public static final String GET_ITEMS = "getItems";
    }


    public static class LeadsSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String GET_LEADS_IN_BATCHES = "getLeadsInBatches";
        public static final String GET_LEAD_DETAILS_BY_ID = "getLeadDetailsById";
        public static final String GET_LEAD_DETAILS_BY_EMAIL = "getLeadDetailsByEmail";
        public static final String CREATE_LEAD = "createLead";
        public static final String UPDATE_LEAD = "updateLead";
        public static final String TOGGLE_LEAD = "toggleLead";
    }

    public static class PurchaseOrderSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String GET_PURCHASE_ORDERS_IN_BATCHES = "getPurchaseOrdersInBatches";
        public static final String GET_PURCHASE_ORDER_BY_ID = "getPurchaseOrderById";
        public static final String CREATE_PURCHASE_ORDER = "createPurchaseOrder";
        public static final String UPDATE_PURCHASE_ORDER = "updatePurchaseOrder";
        public static final String TOGGLE_PURCHASE_ORDER = "togglePurchaseOrder";
        public static final String APPROVED_BY_PURCHASE_ORDER = "approvedByPurchaseOrder";
        public static final String GET_PURCHASE_ORDER_PDF = "getPurchaseOrderPdf";
    }

    public static class SalesOrderSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";
        public static final String GET_PACKAGING_ESTIMATE = "getPackagingEstimate";
        public static final String GET_SHIPPING_ESTIMATE = "getShippingEstimate";

        // Api + frontend Endpoints
        public static final String GET_SALES_ORDERS_IN_BATCHES = "getSalesOrdersInBatches";
        public static final String GET_SALES_ORDER_BY_ID = "getSalesOrderById";
        public static final String CREATE_SALES_ORDER = "createSalesOrder";
        public static final String UPDATE_SALES_ORDER = "updateSalesOrder";
        public static final String TOGGLE_SALES_ORDER = "toggleSalesOrder";
        public static final String GET_SALES_ORDER_PDF = "getSalesOrderPdf";
        public static final String CANCEL_SALES_ORDER = "cancelSalesOrder";

        public static final String UPDATE_CUSTOMER_DELIVERY_ADDRESS = "updateCustomerDeliveryAddress";
        public static final String UPDATE_SALES_ORDER_PICKUP_ADDRESS = "updateSalesOrderPickupAddress";
    }

    public static class WebTemplateSubRoute {
        // purely frontend endpoints
        public static final String SET_INCLUDE_DELETED = "setIncludeDeleted";
        public static final String GET_INCLUDE_DELETED = "getIncludeDeleted";

        // Api + frontend Endpoints
        public static final String INSERT_WEB_TEMPLATE = "insertWebTemplate";
        public static final String GET_WEB_TEMPLATES_IN_BATCHES = "getWebTemplatesInBatches";
        public static final String UPDATE_WEB_TEMPLATE = "updateWebTemplate";
        public static final String DEPLOY_WEB_TEMPLATE = "deployWebTemplate";
        public static final String TOGGLE_WEB_TEMPLATE = "toggleWebTemplate";
        public static final String GET_WEB_TEMPLATE_BY_ID = "getWebTemplateById";

        // WebTemplate endpoints
        public static final String UPDATE_USER_CART = "updateUserCart";
        public static final String UPDATE_USER_LIKED_ITEMS = "updateUserLikedItems";
        public static final String GET_WEB_TEMPLATE_BY_WILDCARD = "getWebTemplateByWildCard";
    }

    public static class ProductReviewSubRoute {
        public static final String INSERT_PRODUCT_REVIEW = "insertProductReview";
        public static final String GET_PRODUCT_REVIEWS_IN_BATCHES_GIVEN_PRODUCT_ID = "getProductReviewsInBatchesGivenProductId";
        public static final String TOGGLE_PRODUCT_REVIEW = "toggleProductReview";
        public static final String SET_PRODUCT_REVIEW_SCORE = "setProductReviewScore";
    }

    public static class DataSubRoute {
        // purely frontend endpoints
        public static final String GET_COLORS = "getColors";
        public static final String GET_STATES = "getStates";
        public static final String GET_ROLES = "getRoles";
        public static final String GET_TIMEZONES = "getTimeZones";
        public static final String GET_LEAD_STATUSES = "getLeadStatuses";
        public static final String GET_PRIORITY_STATUSES = "getPriorityStatuses";
        public static final String GET_PAYMENT_OPTIONS = "getPaymentOptions";
        public static final String GET_SCHEDULER_EVENT_TYPES_OPTIONS = "getSchedulerEventTypesOptions";
        public static final String GET_FILTER_OPTIONS = "getFilterOptions";
        public static final String GET_SORT_OPTIONS = "getSortOptions";
        public static final String GET_STATE_CITY_MAPPING = "getStateCityMappingOptions";
        public static final String GET_FONT_STYLES = "getFontStyles";
        public static final String FIND_CATEGORIES_WITHOUT_CHILDREN = "findCategoriesWithoutChildren";
    }

    public static class BulkSubRoute {
        public static final String BULK_INSERT = "bulkInsert";
        public static final String GENERATE_BULK_IMPORT_EXCEL = "generateBulkImportExcel";
    }
}