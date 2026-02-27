package com.example.SpringApi.ModelTests.Core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.SpringApi.Models.Authorizations;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class AuthorizationsContractTest {

  // Total Tests: 2
  @Test
  void authorizations_privateConstructor_isInvocable() throws Exception {
    Constructor<Authorizations> constructor = Authorizations.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Authorizations instance = constructor.newInstance();

    assertNotNull(instance);
  }

  @Test
  void authorizations_publicStaticStringConstants_areNotBlank() throws Exception {
    for (Field field : Authorizations.class.getDeclaredFields()) {
      if (!Modifier.isPublic(field.getModifiers())
          || !Modifier.isStatic(field.getModifiers())
          || !Modifier.isFinal(field.getModifiers())
          || field.getType() != String.class) {
        continue;
      }
      field.setAccessible(true);
      Object value = field.get(null);
      assertNotNull(value, "Null constant: " + field.getName());
      assertFalse(value.toString().isBlank(), "Blank constant: " + field.getName());
    }
  }
}
