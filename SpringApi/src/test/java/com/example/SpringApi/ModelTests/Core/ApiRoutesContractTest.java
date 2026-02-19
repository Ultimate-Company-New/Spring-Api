package com.example.SpringApi.ModelTests.Core;

import com.example.SpringApi.Models.ApiRoutes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiRoutesContractTest {

    // Total Tests: 3
    @Test
    void apiRoutes_privateConstructors_areInvocableForStaticClasses() throws Exception {
        ArrayDeque<Class<?>> queue = new ArrayDeque<>();
        queue.add(ApiRoutes.class);

        while (!queue.isEmpty()) {
            Class<?> current = queue.removeFirst();
            for (Class<?> nested : current.getDeclaredClasses()) {
                queue.addLast(nested);
            }

            if (current.isEnum() || current.isInterface()) {
                continue;
            }
            if (current.isMemberClass() && !Modifier.isStatic(current.getModifiers())) {
                continue;
            }

            try {
                Constructor<?> constructor = current.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object instance = constructor.newInstance();
                assertNotNull(instance);
            } catch (NoSuchMethodException noSuchMethodException) {
                // Ignore classes without no-arg constructors.
            }
        }
    }

    @Test
    void apiRoutes_publicStaticStringConstants_areNotBlankRecursively() throws Exception {
        ArrayDeque<Class<?>> queue = new ArrayDeque<>();
        queue.add(ApiRoutes.class);

        while (!queue.isEmpty()) {
            Class<?> current = queue.removeFirst();
            for (Class<?> nested : current.getDeclaredClasses()) {
                queue.addLast(nested);
            }

            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isPublic(field.getModifiers())
                        || !Modifier.isStatic(field.getModifiers())
                        || !Modifier.isFinal(field.getModifiers())
                        || field.getType() != String.class) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(null);
                assertNotNull(value, "Null constant: " + current.getSimpleName() + "." + field.getName());
                assertFalse(value.toString().isBlank(),
                        "Blank constant: " + current.getSimpleName() + "." + field.getName());
            }
        }
    }

    @Test
    void apiRoutes_enums_defineConstants() {
        assertTrue(ApiRoutes.OrdersSubRoute.values().length > 0);
    }
}
