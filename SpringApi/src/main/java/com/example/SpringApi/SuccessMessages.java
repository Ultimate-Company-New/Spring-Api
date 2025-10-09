package com.example.SpringApi;

public class SuccessMessages {
    public static final String Success = "Success.";

    public static class EventSuccessMessages {
        // standard success messages
        public static final String GetEvents = "Successfully got events.";
        public static final String InsertEvent = "Successfully inserted event.";
        public static final String UpdateEvent = "Successfully updated event.";
        public static final String GetEvent = "Successfully got event details.";
        public static final String ToggleEvent = "Successfully toggled event.";
    }

    public static class CarrierSuccessMessages {
        // standard success messages
        public static final String GetCarrier = "Successfully got carriers details.";
        public static final String UpdatedCarrier = "Successfully updated carrier details.";
    }

    public static class LoginSuccessMessages {
        // standard success messages
        public static final String UserEmailConfirmed = "User Email confirmed please log in to the platform to continue using the application.";
        public static final String SuccessSignIn = "Successfully signed in.";
        public static final String SuccessSignedUp = "Successfully signed up.";
    }

    public static class AddressSuccessMessages{
        // standard success messages
        public static final String InsertAddress = "Successfully inserted address.";
        public static final String UpdateAddress = "Successfully updated address.";
        public static final String ToggleAddress = "Successfully deleted address.";
        public static final String GetAddress = "Successfully got address details.";
    }

    public static class ClientSuccessMessages{
        // standard success messages
        public static final String InsertClient = "Successfully inserted client.";
        public static final String CreateClient = "Successfully created client.";
        public static final String UpdateClient = "Successfully updated client.";
        public static final String ToggleClient = "Successfully toggled client.";
        public static final String GetClient = "Successfully got client details.";
        public static final String GetClients = "Successfully got clients.";
    }

    public static class UserGroupSuccessMessages{
        // standard success messages
        public static final String InsertGroup = "Successfully inserted group.";
        public static final String UpdateGroup = "Successfully updated group.";
        public static final String ToggleGroup = "Successfully toggled group.";
        public static final String GetGroup = "Successfully got group details.";
        public static final String GetGroups = "Successfully got groups";

        // additional Success messages
        public static final String SUCC001 = "Successfully got user's from the from the given groupid.";
    }

    public static class TodoSuccessMessages{
        // standard success messages
        public static final String InsertTodo = "Successfully inserted todo.";
        public static final String UpdateTodo = "Successfully updated todo.";
        public static final String DeleteTodo = "Successfully deleted toto.";
        public static final String GetTodoItems = "Successfully got todo items for the current user";
        public static final String ToggleTodo = "Successfully toggled todo.";
    }

    public static class MessagesSuccessMessages{
        // standard success messages
        public static final String InsertMessage = "Successfully inserted message.";
        public static final String UpdateMessage = "Successfully updated message.";
        public static final String GetMessages = "Successfully got messages.";
        public static final String ToggleMessage = "Successfully got messages.";
        public static final String GotMessageDetails = "Successfully got message details.";
    }

    public static class WebTemplatesSuccessMessages{
        // standard success messages
        public static final String InsertWebTemplate = "Successfully inserted web template.";
        public static final String UpdateWebTemplate = "Successfully updated web template.";
        public static final String GetWebTemplate = "Successfully got web template.";
        public static final String ToggleWebTemplate = "Successfully toggled web template.";

        public static final String UpdateUserCart = "Successfully updated user cart.";
        public static final String UpdateUserLikedItems = "Successfully updated user liked items.";
    }

    public static class PickupLocationSuccessMessages{
        // standard success messages
        public static final String InsertPickupLocation = "Successfully inserted pickup location.";
        public static final String UpdatePickupLocation = "Successfully updated pickup location.";
        public static final String GetPickupLocation = "Successfully got pickup location.";
        public static final String TogglePickupLocation = "Successfully toggled pickup location.";
    }

    public static class PromoSuccessMessages{
        // standard success messages
        public static final String InsertPromo = "Successfully inserted promo code.";
        public static final String UpdatePromo= "Successfully updated promo code.";
        public static final String GetPromo = "Successfully got promo code.";
        public static final String TogglePromo = "Successfully toggled promo code.";
        public static final String CreatePromo = "Successfully created promo ";
        public static final String ToggledPromo = "Successfully toggled promo ";
    }

    public static class ProductCategorySuccessMessages{
        // standard success messages
        public static final String GetProductCategories = "Successfully got product categories";
    }

    public static class ProductsSuccessMessages{
        // standard success messages
        public static final String InsertProduct = "Successfully inserted product.";
        public static final String UpdateProduct= "Successfully updated product.";
        public static final String GetProduct = "Successfully got product(s).";
        public static final String ToggleProduct = "Successfully toggled product.";
        public static final String ToggleReturnProduct = "Successfully toggled product return.";
    }

    public static class LeadSuccessMessages {
        // Standard success messages for lead operations
        public static final String InsertLead = "Successfully inserted lead.";
        public static final String UpdateLead = "Successfully updated lead.";
        public static final String GetLead = "Successfully retrieved lead details.";
        public static final String ToggleLead = "Successfully toggled lead.";
    }

    public static class SalesOrderSuccessMessages {
        // Standard success messages for sales order operations
        public static final String InsertSalesOrder = "Successfully inserted sales order.";
        public static final String UpdateSalesOrder = "Successfully updated sales order.";
        public static final String GetSalesOrder = "Successfully retrieved sales order details.";
        public static final String ToggleSalesOrder = "Successfully toggled sales order.";
        public static final String GetSalesOrderPdf = "Successfully retrieved sales order pdf.";

        public static final String UpdatedPickupLocation = "Successfully updated pickup location for the given order.";
        public static final String UpdatedCustomerDeliveryAddress = "Successfully updated customer delivery address on entire sales order.";
        public static final String CancelOrder = "Successfully cancelled sales order.";
    }

    public static class PurchaseOrderSuccessMessages {
        // Standard success messages for purchase order operations
        public static final String InsertPurchaseOrder = "Successfully inserted purchase order.";
        public static final String UpdatePurchaseOrder = "Successfully updated purchase order.";
        public static final String GetPurchaseOrder = "Successfully retrieved purchase order details.";
        public static final String TogglePurchaseOrder = "Successfully toggled purchase order.";
        public static final String SetApprovedByPurchaseOrder = "Successfully updated the approved by user id for the given purchase order.";
        public static final String GetPurchaseOrderPdf = "Successfully retrieved purchase order pdf.";
    }

    public static class ProductReviewSuccessMessages {
        // Standard success messages for product review operations
        public static final String InsertProductReview = "Successfully inserted product review.";
        public static final String UpdateProductReview = "Successfully updated product review.";
        public static final String ToggleProductReview = "Successfully toggled product review.";
        public static final String ScoreUpdate = "Successfully updated the review Score.";
        public static final String DeleteProductReview = "Successfully deleted the product review.";
    }

    public static class PaymentInfoSuccessMessages {
        // Standard success messages for payment information operations
        public static final String InsertPaymentInfo = "Successfully inserted payment information.";
        public static final String UpdatePaymentInfo = "Successfully updated payment information.";
        public static final String GetPaymentInfo = "Successfully retrieved payment information.";
        public static final String TogglePaymentInfo = "Successfully toggled payment information status.";
    }

    public static class PackagesSuccessMessages {
        // Standard success messages for payment information operations
        public static final String InsertPackage = "Successfully inserted package in the system.";
        public static final String UpdatePackage = "Successfully updated package in the system.";
        public static final String GetPackage = "Successfully retrieved package information.";
        public static final String TogglePackage = "Successfully toggled package status.";
    }

    public static class SupportSuccessMessages {
        // Standard success messages for payment information operations
        public static final String GetAttachment = "Successfully got attachments in the ticket.";
        public static final String CreateTicket = "Successfully created ticket.";
    }

    public static class UserGridPreferenceSuccessMessages {
        // standard success messages
        public static final String UpdateUserGridPreference = "Successfully updated user grid preferences.";
        public static final String GetUserGridPreference = "Successfully got grid preferences.";
    }
}