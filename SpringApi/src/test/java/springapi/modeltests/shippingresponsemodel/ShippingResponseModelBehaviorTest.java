package springapi.modeltests.shippingresponsemodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.responsemodels.ShippingCalculationResponseModel;
import springapi.models.shippingresponsemodel.GetAllPickupLocationsResponseModel;
import springapi.models.shippingresponsemodel.ShipRocketAwbResponseModel;
import springapi.models.shippingresponsemodel.ShipRocketOrderDetailsResponseModel;
import springapi.models.shippingresponsemodel.ShipRocketTrackingResponseModel;
import springapi.models.shippingresponsemodel.ShippingOptionsResponseModel;

@DisplayName("Shipping Response Model Behavior Tests")
class ShippingResponseModelBehaviorTest {

  // Total Tests: 6

  /**
   * Purpose: Verify AWB response helper methods resolve success and nested values safely. Expected
   * Result: isSuccess/getAwbCode/getShipmentId follow nested-null and happy-path behavior.
   * Assertions: True only for assign status 1 and nested values are returned when available.
   */
  @Test
  @DisplayName("shipRocketAwbResponseModel - Helper Methods Branches - Success")
  void shipRocketAwbResponseModel_s01_helperMethodsBranches_success() {
    // Arrange
    ShipRocketAwbResponseModel model = new ShipRocketAwbResponseModel();
    ShipRocketAwbResponseModel.AwbResponse response = new ShipRocketAwbResponseModel.AwbResponse();
    ShipRocketAwbResponseModel.AwbData data = new ShipRocketAwbResponseModel.AwbData();
    data.setAwbCode("AWB-100");
    data.setShipmentId(200L);
    response.setData(data);
    model.setResponse(response);

    // Act/Assert
    model.setAwbAssignStatus(1);
    assertTrue(model.isSuccess());
    assertEquals("AWB-100", model.getAwbCode());
    assertEquals(200L, model.getShipmentId());

    model.setAwbAssignStatus(0);
    assertFalse(model.isSuccess());

    model.setResponse(null);
    assertEquals(null, model.getAwbCode());
    assertEquals(null, model.getShipmentId());
  }

  /**
   * Purpose: Verify tracking response helper evaluates success correctly. Expected Result: Returns
   * true only when trackingData exists and trackStatus is 1. Assertions: Null and non-1 status are
   * false, status 1 is true.
   */
  @Test
  @DisplayName("shipRocketTrackingResponseModel - IsSuccess Branches - Success")
  void shipRocketTrackingResponseModel_s02_isSuccessBranches_success() {
    // Arrange
    ShipRocketTrackingResponseModel model = new ShipRocketTrackingResponseModel();
    ShipRocketTrackingResponseModel.TrackingData trackingData =
        new ShipRocketTrackingResponseModel.TrackingData();

    // Act/Assert
    assertFalse(model.isSuccess());

    model.setTrackingData(trackingData);
    assertFalse(model.isSuccess());

    trackingData.setTrackStatus(0);
    assertFalse(model.isSuccess());

    trackingData.setTrackStatus(1);
    assertTrue(model.isSuccess());
  }

  /**
   * Purpose: Exercise nested pickup-location response beans to execute generated accessors.
   * Expected Result: All readable/writable properties round-trip assigned values. Assertions:
   * Property setter and getter values match for each exercised bean.
   */
  @Test
  @DisplayName("shippingResponseModels - PickupLocation Nested Beans RoundTrip - Success")
  void shippingResponseModels_s03_pickupLocationNestedBeansRoundTrip_success() throws Exception {
    // Arrange/Act/Assert
    exerciseBeanProperties(new GetAllPickupLocationsResponseModel.Data());
    exerciseBeanProperties(new GetAllPickupLocationsResponseModel.ShippingAddress());
  }

  /**
   * Purpose: Exercise nested order-details response beans to execute generated accessors. Expected
   * Result: Properties are set and read back consistently for all nested detail types. Assertions:
   * Setter/getter round-trip behavior matches sample values.
   */
  @Test
  @DisplayName("shippingResponseModels - OrderDetails Nested Beans RoundTrip - Success")
  void shippingResponseModels_s04_orderDetailsNestedBeansRoundTrip_success() throws Exception {
    // Arrange/Act/Assert
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.OrderDetailsData());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.OrderProduct());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.ShipmentDetails());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.AwbData());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.AwbCharges());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.OrderInsurance());
    exerciseBeanProperties(new ShipRocketOrderDetailsResponseModel.ReturnPickupData());
  }

  /**
   * Purpose: Exercise tracking/options nested response beans to improve accessor-path coverage.
   * Expected Result: Properties round-trip correctly for all nested beans. Assertions:
   * Setter/getter pairs return the assigned sample values.
   */
  @Test
  @DisplayName("shippingResponseModels - TrackingAndOptions Nested Beans RoundTrip - Success")
  void shippingResponseModels_s05_trackingAndOptionsNestedBeansRoundTrip_success()
      throws Exception {
    // Arrange/Act/Assert
    exerciseBeanProperties(new ShipRocketTrackingResponseModel.TrackingData());
    exerciseBeanProperties(new ShipRocketTrackingResponseModel.ShipmentTrack());
    exerciseBeanProperties(new ShipRocketTrackingResponseModel.TrackActivity());
    exerciseBeanProperties(new ShipRocketTrackingResponseModel.QcResponse());
    exerciseBeanProperties(new ShippingOptionsResponseModel.AvailableCourierCompany());
    exerciseBeanProperties(new ShippingOptionsResponseModel.SuppressionDates());
    exerciseBeanProperties(new ShippingOptionsResponseModel.CovidZones());
    exerciseBeanProperties(new ShippingOptionsResponseModel.Data());
    exerciseBeanProperties(new ShippingOptionsResponseModel.RecommendedBy());
    exerciseBeanProperties(new ShipRocketAwbResponseModel.ShippedBy());
  }

  /**
   * Purpose: Exercise shipping calculation nested beans beyond mapper coverage. Expected Result:
   * CourierOption and LocationShippingOptions bean properties round-trip assigned values.
   * Assertions: Generated accessors for both nested classes execute successfully.
   */
  @Test
  @DisplayName("shippingResponseModels - ShippingCalculation Nested Beans RoundTrip - Success")
  void shippingResponseModels_s06_shippingCalculationNestedBeansRoundTrip_success()
      throws Exception {
    // Arrange/Act/Assert
    exerciseBeanProperties(new ShippingCalculationResponseModel.CourierOption());
    exerciseBeanProperties(new ShippingCalculationResponseModel.LocationShippingOptions());
  }

  private void exerciseBeanProperties(Object bean) throws Exception {
    PropertyDescriptor[] descriptors =
        Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors();
    assertTrue(descriptors.length > 0);

    for (PropertyDescriptor descriptor : descriptors) {
      Method writeMethod = descriptor.getWriteMethod();
      Method readMethod = descriptor.getReadMethod();
      if (writeMethod == null || readMethod == null) {
        continue;
      }
      if (Modifier.isStatic(writeMethod.getModifiers())
          || Modifier.isStatic(readMethod.getModifiers())) {
        continue;
      }

      Class<?> parameterType = writeMethod.getParameterTypes()[0];
      Object sample = sampleValue(parameterType);

      writeMethod.setAccessible(true);
      writeMethod.invoke(bean, sample);

      readMethod.setAccessible(true);
      Object actual = readMethod.invoke(bean);
      assertEquals(sample, actual);
    }
  }

  private Object sampleValue(Class<?> type) {
    if (type == String.class) {
      return "value";
    }
    if (type == int.class || type == Integer.class) {
      return 7;
    }
    if (type == long.class || type == Long.class) {
      return 7L;
    }
    if (type == double.class || type == Double.class) {
      return 7.5d;
    }
    if (type == float.class || type == Float.class) {
      return 7.5f;
    }
    if (type == boolean.class || type == Boolean.class) {
      return Boolean.TRUE;
    }
    if (List.class.isAssignableFrom(type)) {
      return new ArrayList<>();
    }
    if (Map.class.isAssignableFrom(type)) {
      return new LinkedHashMap<>();
    }
    if (type == Object.class) {
      return new Object();
    }
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }

    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }
}
