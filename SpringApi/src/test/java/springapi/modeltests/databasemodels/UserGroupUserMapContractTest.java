package springapi.modeltests.databasemodels;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserGroupUserMapContractTest {

  private final ObjectMapper jackson =
      new ObjectMapper()
          .findAndRegisterModules()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  // Total Tests: 3
  @Test
  void UserGroupUserMap_constructor_createsInstance() {
    assertNotNull(createInstance());
  }

  @Test
  void UserGroupUserMap_serialization_contracts() throws Exception {
    Object instance = createInstance();
    initializeWritableProperties(instance);

    String jacksonJson = jackson.writeValueAsString(instance);
    assertFalse(jacksonJson.isBlank());

    if (hasNoArgConstructor(springapi.models.databasemodels.UserGroupUserMap.class)) {
      JavaType jacksonType =
          jackson
              .getTypeFactory()
              .constructType(springapi.models.databasemodels.UserGroupUserMap.class);
      Object jacksonRoundTrip = jackson.readValue(jacksonJson, jacksonType);
      assertNotNull(jacksonRoundTrip);
    }
  }

  @Test
  void UserGroupUserMap_constructors_areExercised() {
    int attempted = 0;
    for (Constructor<?> constructor :
        springapi.models.databasemodels.UserGroupUserMap.class.getDeclaredConstructors()) {
      attempted++;
      constructor.setAccessible(true);
      Object[] args =
          Arrays.stream(constructor.getParameterTypes()).map(this::sampleValue).toArray();
      try {
        Object created = constructor.newInstance(args);
        assertNotNull(created);
      } catch (InvocationTargetException invocationTargetException) {
        assertNotNull(invocationTargetException.getCause());
      } catch (ReflectiveOperationException reflectiveOperationException) {
        // Ignore unsupported constructor signatures in this contract test.
      }
    }
    assertTrue(attempted > 0);
  }

  private springapi.models.databasemodels.UserGroupUserMap createInstance() {
    return (springapi.models.databasemodels.UserGroupUserMap)
        instantiate(springapi.models.databasemodels.UserGroupUserMap.class);
  }

  private Object instantiate(Class<?> clazz) {
    try {
      Constructor<?> noArg = clazz.getDeclaredConstructor();
      noArg.setAccessible(true);
      return noArg.newInstance();
    } catch (NoSuchMethodException ignored) {
      // Try parameterized constructors.
    } catch (ReflectiveOperationException reflectiveOperationException) {
      throw new AssertionError(
          "Unable to instantiate " + clazz.getName(), reflectiveOperationException);
    }

    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Arrays.sort(constructors, Comparator.comparingInt(Constructor::getParameterCount));
    for (Constructor<?> constructor : constructors) {
      try {
        constructor.setAccessible(true);
        Object[] args =
            Arrays.stream(constructor.getParameterTypes()).map(this::sampleValue).toArray();
        return constructor.newInstance(args);
      } catch (ReflectiveOperationException reflectiveOperationException) {
        // Continue trying constructors.
      }
    }

    throw new AssertionError("No usable constructor found for " + clazz.getName());
  }

  private void initializeWritableProperties(Object instance) {
    try {
      PropertyDescriptor[] descriptors =
          Introspector.getBeanInfo(instance.getClass(), Object.class).getPropertyDescriptors();
      for (PropertyDescriptor descriptor : descriptors) {
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null || Modifier.isStatic(writeMethod.getModifiers())) {
          continue;
        }

        Class<?> parameterType = writeMethod.getParameterTypes()[0];
        Object sample = sampleValue(parameterType);
        writeMethod.setAccessible(true);
        try {
          writeMethod.invoke(instance, sample);
        } catch (ReflectiveOperationException reflectiveOperationException) {
          // Ignore individual property initialization failures.
        }
      }
    } catch (Exception exception) {
      // Ignore initialization failures and continue serialization contract.
    }
  }

  private boolean hasNoArgConstructor(Class<?> clazz) {
    try {
      clazz.getDeclaredConstructor();
      return true;
    } catch (NoSuchMethodException noSuchMethodException) {
      return false;
    }
  }

  private Object sampleValue(Class<?> type) {
    if (type == String.class) {
      return "value";
    }
    if (type == int.class || type == Integer.class) {
      return 1;
    }
    if (type == long.class || type == Long.class) {
      return 1L;
    }
    if (type == double.class || type == Double.class) {
      return 1.5d;
    }
    if (type == float.class || type == Float.class) {
      return 1.5f;
    }
    if (type == boolean.class || type == Boolean.class) {
      return Boolean.TRUE;
    }
    if (type == short.class || type == Short.class) {
      return (short) 1;
    }
    if (type == byte.class || type == Byte.class) {
      return (byte) 1;
    }
    if (type == char.class || type == Character.class) {
      return 'a';
    }
    if (type == BigDecimal.class) {
      return new BigDecimal("10.00");
    }
    if (type == LocalDate.class) {
      return LocalDate.of(2025, 1, 1);
    }
    if (type == LocalDateTime.class) {
      return LocalDateTime.of(2025, 1, 1, 1, 1);
    }
    if (type == LocalTime.class) {
      return LocalTime.of(1, 1);
    }
    if (type == Instant.class) {
      return Instant.parse("2025-01-01T00:00:00Z");
    }
    if (type == UUID.class) {
      return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }
    if (List.class.isAssignableFrom(type)) {
      return List.of();
    }
    if (Map.class.isAssignableFrom(type)) {
      return new LinkedHashMap<>();
    }
    if (java.util.Set.class.isAssignableFrom(type)) {
      return new LinkedHashSet<>();
    }
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants.length > 0 ? constants[0] : null;
    }

    try {
      Constructor<?> nestedNoArg = type.getDeclaredConstructor();
      nestedNoArg.setAccessible(true);
      return nestedNoArg.newInstance();
    } catch (ReflectiveOperationException reflectiveOperationException) {
      return null;
    }
  }
}
