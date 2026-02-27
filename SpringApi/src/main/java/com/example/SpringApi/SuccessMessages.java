package com.example.SpringApi;

public class SuccessMessages {
  private SuccessMessages() {}

  public static final String SUCCESS = "Success.";

  public static class EventSuccessMessages {
    private EventSuccessMessages() {}

    // standard success messages
    public static final String GET_EVENTS = "Successfully got events.";
    public static final String INSERT_EVENT = "Successfully inserted event.";
    public static final String UPDATE_EVENT = "Successfully updated event.";
    public static final String GET_EVENT = "Successfully got event details.";
    public static final String TOGGLE_EVENT = "Successfully toggled event.";
  }

  public static class CarrierSuccessMessages {
    private CarrierSuccessMessages() {}

    // standard success messages
    public static final String GET_CARRIER = "Successfully got carriers details.";
    public static final String UPDATED_CARRIER = "Successfully updated carrier details.";
  }

  public static class LoginSuccessMessages {
    private LoginSuccessMessages() {}

    // standard success messages
    public static final String USER_EMAIL_CONFIRMED =
        "User Email confirmed please log in to the platform to continue using the application.";
    public static final String SUCCESS_SIGN_IN = "Successfully signed in.";
    public static final String SUCCESS_SIGNED_UP = "Successfully signed up.";
  }

  public static class AddressSuccessMessages {
    private AddressSuccessMessages() {}

    // standard success messages
    public static final String INSERT_ADDRESS = "Successfully inserted address.";
    public static final String UPDATE_ADDRESS = "Successfully updated address.";
    public static final String TOGGLE_ADDRESS = "Successfully deleted address.";
    public static final String GET_ADDRESS = "Successfully got address details.";
  }

  public static class ClientSuccessMessages {
    private ClientSuccessMessages() {}

    // standard success messages
    public static final String INSERT_CLIENT = "Successfully inserted client.";
    public static final String CREATE_CLIENT = "Successfully created client.";
    public static final String UPDATE_CLIENT = "Successfully updated client.";
    public static final String TOGGLE_CLIENT = "Successfully toggled client.";
    public static final String GET_CLIENT = "Successfully got client details.";
    public static final String GET_CLIENTS = "Successfully got clients.";
  }

  public static class UserGroupSuccessMessages {
    private UserGroupSuccessMessages() {}

    // standard success messages
    public static final String INSERT_GROUP = "Successfully inserted group.";
    public static final String UPDATE_GROUP = "Successfully updated group.";
    public static final String TOGGLE_GROUP = "Successfully toggled group.";
    public static final String GET_GROUP = "Successfully got group details.";
    public static final String GET_GROUPS = "Successfully got groups";

    // additional Success messages
    public static final String SUCC001 = "Successfully got user's from the from the given groupid.";
  }

  public static class TodoSuccessMessages {
    private TodoSuccessMessages() {}

    // standard success messages
    public static final String INSERT_TODO = "Successfully inserted todo.";
    public static final String UPDATE_TODO = "Successfully updated todo.";
    public static final String DELETE_TODO = "Successfully deleted toto.";
    public static final String GET_TODO_ITEMS = "Successfully got todo items for the current user";
    public static final String TOGGLE_TODO = "Successfully toggled todo.";
  }

  public static class MessagesSuccessMessages {
    private MessagesSuccessMessages() {}

    // standard success messages
    public static final String INSERT_MESSAGE = "Successfully inserted message.";
    public static final String UPDATE_MESSAGE = "Successfully updated message.";
    public static final String GET_MESSAGES = "Successfully got messages.";
    public static final String TOGGLE_MESSAGE = "Successfully toggled message.";
    public static final String GOT_MESSAGE_DETAILS = "Successfully got message details.";
    public static final String SET_MESSAGE_READ = "Successfully marked message as read.";
  }

  public static class WebTemplatesSuccessMessages {
    private WebTemplatesSuccessMessages() {}

    // standard success messages
    public static final String INSERT_WEB_TEMPLATE = "Successfully inserted web template.";
    public static final String UPDATE_WEB_TEMPLATE = "Successfully updated web template.";
    public static final String GET_WEB_TEMPLATE = "Successfully got web template.";
    public static final String TOGGLE_WEB_TEMPLATE = "Successfully toggled web template.";

    public static final String UPDATE_USER_CART = "Successfully updated user cart.";
    public static final String UPDATE_USER_LIKED_ITEMS = "Successfully updated user liked items.";
  }

  public static class PickupLocationSuccessMessages {
    private PickupLocationSuccessMessages() {}

    // standard success messages
    public static final String INSERT_PICKUP_LOCATION = "Successfully inserted pickup location.";
    public static final String UPDATE_PICKUP_LOCATION = "Successfully updated pickup location.";
    public static final String GET_PICKUP_LOCATION = "Successfully got pickup location.";
    public static final String TOGGLE_PICKUP_LOCATION = "Successfully toggled pickup location.";
  }

  public static class PromoSuccessMessages {
    private PromoSuccessMessages() {}

    // standard success messages
    public static final String INSERT_PROMO = "Successfully inserted promo code.";
    public static final String UPDATE_PROMO = "Successfully updated promo code.";
    public static final String GET_PROMO = "Successfully got promo code.";
    public static final String TOGGLE_PROMO = "Successfully toggled promo code.";
    public static final String CREATE_PROMO = "Successfully created promo ";
    public static final String TOGGLED_PROMO = "Successfully toggled promo ";
  }

  public static class ProductCategorySuccessMessages {
    private ProductCategorySuccessMessages() {}

    // standard success messages
    public static final String GET_PRODUCT_CATEGORIES = "Successfully got product categories";
  }

  public static class ProductsSuccessMessages {
    private ProductsSuccessMessages() {}

    // standard success messages
    public static final String INSERT_PRODUCT = "Successfully inserted product.";
    public static final String UPDATE_PRODUCT = "Successfully updated product.";
    public static final String GET_PRODUCT = "Successfully got product(s).";
    public static final String TOGGLE_PRODUCT = "Successfully toggled product.";
    public static final String TOGGLE_RETURN_PRODUCT = "Successfully toggled product return.";
  }

  public static class LeadSuccessMessages {
    private LeadSuccessMessages() {}

    // Standard success messages for lead operations
    public static final String INSERT_LEAD = "Successfully inserted lead.";
    public static final String UPDATE_LEAD = "Successfully updated lead.";
    public static final String GET_LEAD = "Successfully retrieved lead details.";
    public static final String TOGGLE_LEAD = "Successfully toggled lead.";
  }

  public static class PurchaseOrderSuccessMessages {
    private PurchaseOrderSuccessMessages() {}

    // Standard success messages for purchase order operations
    public static final String INSERT_PURCHASE_ORDER = "Successfully inserted purchase order.";
    public static final String UPDATE_PURCHASE_ORDER = "Successfully updated purchase order.";
    public static final String GET_PURCHASE_ORDER =
        "Successfully retrieved purchase order details.";
    public static final String TOGGLE_PURCHASE_ORDER = "Successfully toggled purchase order.";
    public static final String SET_APPROVED_BY_PURCHASE_ORDER =
        "Successfully updated the approved by user id for the given purchase order.";
    public static final String SET_REJECTED_BY_PURCHASE_ORDER =
        "Successfully updated the rejected by user id for the given purchase order.";
    public static final String GET_PURCHASE_ORDER_PDF =
        "Successfully retrieved purchase order pdf.";
  }

  public static class ProductReviewSuccessMessages {
    private ProductReviewSuccessMessages() {}

    // Standard success messages for product review operations
    public static final String INSERT_PRODUCT_REVIEW = "Successfully inserted product review.";
    public static final String UPDATE_PRODUCT_REVIEW = "Successfully updated product review.";
    public static final String TOGGLE_PRODUCT_REVIEW = "Successfully toggled product review.";
    public static final String SCORE_UPDATE = "Successfully updated the review Score.";
    public static final String DELETE_PRODUCT_REVIEW = "Successfully deleted the product review.";
  }

  public static class PackagesSuccessMessages {
    private PackagesSuccessMessages() {}

    // Standard success messages for payment information operations
    public static final String INSERT_PACKAGE = "Successfully inserted package in the system.";
    public static final String UPDATE_PACKAGE = "Successfully updated package in the system.";
    public static final String GET_PACKAGE = "Successfully retrieved package information.";
    public static final String TOGGLE_PACKAGE = "Successfully toggled package status.";
  }

  public static class SupportSuccessMessages {
    private SupportSuccessMessages() {}

    // Standard success messages for payment information operations
    public static final String GET_ATTACHMENT = "Successfully got attachments in the ticket.";
    public static final String CREATE_TICKET = "Successfully created ticket.";
  }

  public static class UserSuccessMessages {
    private UserSuccessMessages() {}

    // standard success messages
    public static final String CREATE_USER = "Successfully created user.";
    public static final String UPDATE_USER = "Successfully updated user.";
    public static final String TOGGLE_USER = "Successfully toggled user.";
    public static final String GET_USER = "Successfully got user details.";
    public static final String GET_USERS = "Successfully got users.";
    public static final String EMAIL_CONFIRMED = "Successfully confirmed email.";
  }

  public static class UserGridPreferenceSuccessMessages {
    private UserGridPreferenceSuccessMessages() {}

    // standard success messages
    public static final String UPDATE_USER_GRID_PREFERENCE =
        "Successfully updated user grid preferences.";
    public static final String GET_USER_GRID_PREFERENCE = "Successfully got grid preferences.";
  }
}

