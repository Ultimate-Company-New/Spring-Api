package springapi.ModelTests.Shipping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import springapi.models.requestmodels.CreateReturnRequestModel;
import springapi.models.requestmodels.ShipRocketOrderRequestModel;
import springapi.models.requestmodels.ShipRocketReturnOrderRequestModel;
import springapi.models.requestmodels.ShippingCalculationRequestModel;

class ShippingRequestModelsTest {

  private final Gson gson = new Gson();

  // Total Tests: 7
  @Test
  void shipRocketOrderRequestModel_Serialization_UsesUnderscoreNames() {
    ShipRocketOrderRequestModel model = new ShipRocketOrderRequestModel();
    model.setOrderId("ORD-1");
    model.setPickupLocation("WH-MAIN");
    model.setBillingCustomerName("John");
    model.setShippingIsBilling(Boolean.TRUE);
    model.setOrderItems(
        List.of(new ShipRocketOrderRequestModel.OrderItem("Item1", "SKU1", 2, 500, 10, 18)));
    model.setPaymentMethod("Prepaid");

    String json = gson.toJson(model);

    assertTrue(json.contains("\"order_id\":\"ORD-1\""));
    assertTrue(json.contains("\"pickup_location\":\"WH-MAIN\""));
    assertTrue(json.contains("\"billing_customer_name\":\"John\""));
    assertTrue(json.contains("\"shipping_is_billing\":true"));
    assertTrue(json.contains("\"order_items\":["));
    assertTrue(json.contains("\"selling_price\":500"));
  }

  @Test
  void shipRocketOrderRequestModel_Deserialization_MapsUnderscoreToCamelCase() {
    String json =
        """
                {
                  "order_id":"ORD-2",
                  "pickup_location":"WH-2",
                  "billing_customer_name":"Jane",
                  "shipping_is_billing":false
                }
                """;

    ShipRocketOrderRequestModel parsed = gson.fromJson(json, ShipRocketOrderRequestModel.class);

    assertEquals("ORD-2", parsed.getOrderId());
    assertEquals("WH-2", parsed.getPickupLocation());
    assertEquals("Jane", parsed.getBillingCustomerName());
    assertEquals(Boolean.FALSE, parsed.getShippingIsBilling());
  }

  @Test
  void shipRocketOrderRequestModel_OrderItemConstructor_InitializesAllFields() {
    ShipRocketOrderRequestModel.OrderItem item =
        new ShipRocketOrderRequestModel.OrderItem("Product A", "SKU-A", 3, 1000, 50, 18);

    assertEquals("Product A", item.getName());
    assertEquals("SKU-A", item.getSku());
    assertEquals(3, item.getUnits());
    assertEquals(1000, item.getSellingPrice());
    assertEquals(50, item.getDiscount());
    assertEquals(18, item.getTax());
  }

  @Test
  void shipRocketReturnOrderRequestModel_Serialization_UsesUnderscoreNames() {
    ShipRocketReturnOrderRequestModel model = new ShipRocketReturnOrderRequestModel();
    model.setOrderId("RET-1");
    model.setPickupCustomerName("Alice");
    model.setShippingCustomerName("Warehouse");
    model.setPaymentMethod("Prepaid");

    ShipRocketReturnOrderRequestModel.ReturnOrderItem item =
        new ShipRocketReturnOrderRequestModel.ReturnOrderItem();
    item.setName("Product X");
    item.setQcEnable(Boolean.TRUE);
    item.setSellingPrice(new BigDecimal("99.99"));
    model.setOrderItems(List.of(item));

    String json = gson.toJson(model);

    assertTrue(json.contains("\"order_id\":\"RET-1\""));
    assertTrue(json.contains("\"pickup_customer_name\":\"Alice\""));
    assertTrue(json.contains("\"shipping_customer_name\":\"Warehouse\""));
    assertTrue(json.contains("\"qc_enable\":true"));
    assertTrue(json.contains("\"selling_price\":99.99"));
  }

  @Test
  void shipRocketReturnOrderRequestModel_Deserialization_MapsUnderscoreToCamelCase() {
    String json =
        """
                {
                  "order_id":"RET-2",
                  "pickup_customer_name":"Bob",
                  "pickup_pincode":"400001",
                  "shipping_customer_name":"Main Warehouse"
                }
                """;

    ShipRocketReturnOrderRequestModel parsed =
        gson.fromJson(json, ShipRocketReturnOrderRequestModel.class);

    assertEquals("RET-2", parsed.getOrderId());
    assertEquals("Bob", parsed.getPickupCustomerName());
    assertEquals("400001", parsed.getPickupPincode());
    assertEquals("Main Warehouse", parsed.getShippingCustomerName());
  }

  @Test
  void createReturnRequestModel_JacksonRoundTrip_PreservesNestedFields() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    CreateReturnRequestModel model = new CreateReturnRequestModel();
    model.setShipmentId(700L);
    model.setLength(new BigDecimal("10.5"));
    model.setBreadth(new BigDecimal("5.5"));
    model.setHeight(new BigDecimal("4.5"));
    model.setWeight(new BigDecimal("1.25"));

    CreateReturnRequestModel.ReturnProductItem item =
        new CreateReturnRequestModel.ReturnProductItem();
    item.setProductId(11L);
    item.setQuantity(2);
    item.setReason("DAMAGED");
    item.setComments("Outer package damaged");
    model.setProducts(List.of(item));

    String json = mapper.writeValueAsString(model);
    CreateReturnRequestModel parsed = mapper.readValue(json, CreateReturnRequestModel.class);

    assertEquals(700L, parsed.getShipmentId());
    assertEquals(1, parsed.getProducts().size());
    assertEquals(11L, parsed.getProducts().getFirst().getProductId());
    assertEquals("DAMAGED", parsed.getProducts().getFirst().getReason());
  }

  @Test
  void shippingCalculationRequestModel_JacksonRoundTrip_PreservesPickupLocations()
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ShippingCalculationRequestModel model = new ShippingCalculationRequestModel();
    model.setDeliveryPostcode("400001");
    model.setIsCod(Boolean.TRUE);

    ShippingCalculationRequestModel.PickupLocationShipment shipment =
        new ShippingCalculationRequestModel.PickupLocationShipment();
    shipment.setPickupLocationId(99L);
    shipment.setLocationName("Warehouse A");
    shipment.setPickupPostcode("400002");
    shipment.setTotalWeightKgs(new BigDecimal("2.75"));
    shipment.setTotalQuantity(4);
    shipment.setProductIds(List.of(10L, 11L));
    model.setPickupLocations(List.of(shipment));

    String json = mapper.writeValueAsString(model);
    ShippingCalculationRequestModel parsed =
        mapper.readValue(json, ShippingCalculationRequestModel.class);

    assertEquals("400001", parsed.getDeliveryPostcode());
    assertEquals(Boolean.TRUE, parsed.getIsCod());
    assertEquals(1, parsed.getPickupLocations().size());
    assertEquals(99L, parsed.getPickupLocations().getFirst().getPickupLocationId());
    assertEquals(2, parsed.getPickupLocations().getFirst().getProductIds().size());
  }
}
