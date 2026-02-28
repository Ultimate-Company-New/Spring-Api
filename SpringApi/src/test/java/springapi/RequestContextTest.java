package springapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Request Context Tests")
class RequestContextTest {

  // Total Tests: 4

  /**
   * Purpose: Verify set and get operations in thread-local request context. Expected Result: Stored
   * value is retrievable by key. Assertions: Retrieved value equals stored value.
   */
  @Test
  @DisplayName("requestContext - SetAndGet Value - Success")
  void requestContext_s01_setAndGetValue_success() {
    // Arrange
    RequestContext.clear();

    // Act
    RequestContext.set("user", "nahush");
    String value = RequestContext.get("user");

    // Assert
    assertEquals("nahush", value);
  }

  /**
   * Purpose: Verify getAll exposes all key-value pairs in current context. Expected Result: Context
   * map contains inserted entries. Assertions: Map size and key/value pairs match inserted data.
   */
  @Test
  @DisplayName("requestContext - GetAll ReturnsContextMap - Success")
  void requestContext_s02_getAllReturnsContextMap_success() {
    // Arrange
    RequestContext.clear();
    RequestContext.set("clientId", "1");
    RequestContext.set("loginName", "admin");

    // Act
    Map<String, String> map = RequestContext.getAll();

    // Assert
    assertEquals(2, map.size());
    assertEquals("1", map.get("clientId"));
    assertEquals("admin", map.get("loginName"));
  }

  /**
   * Purpose: Verify clear operation resets thread-local context. Expected Result: Previously stored
   * values are removed. Assertions: Key lookup returns null and map is empty after clear.
   */
  @Test
  @DisplayName("requestContext - Clear RemovesData - Success")
  void requestContext_s03_clearRemovesData_success() {
    // Arrange
    RequestContext.clear();
    RequestContext.set("token", "abc");

    // Act
    RequestContext.clear();
    String value = RequestContext.get("token");
    Map<String, String> map = RequestContext.getAll();

    // Assert
    assertNull(value);
    assertTrue(map.isEmpty());
  }

  /**
   * Purpose: Verify utility-class private constructor is covered. Expected Result: Private
   * constructor can be invoked via reflection. Assertions: Reflected instance is created.
   */
  @Test
  @DisplayName("requestContext - PrivateConstructor Reflection - Success")
  void requestContext_s04_privateConstructorReflection_success() throws Exception {
    // Arrange
    Constructor<RequestContext> constructor = RequestContext.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Act
    RequestContext instance = constructor.newInstance();

    // Assert
    assertNotNull(instance);
  }
}
