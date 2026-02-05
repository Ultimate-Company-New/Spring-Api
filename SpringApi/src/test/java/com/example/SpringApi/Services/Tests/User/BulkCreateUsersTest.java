package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - BulkCreateUsers functionality.
 *
 * Test Summary:
 * | Test Group          | Number of Tests |
 * | :------------------ | :-------------- |
 * | SUCCESS Tests       | 4               |
 * | FAILURE Tests       | 4               |
 * | **Total**           | **8**           |
 */
@DisplayName("UserService - BulkCreateUsers Tests")
class BulkCreateUsersTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("bulkCreateUsers - Verify @PreAuthorize Annotation")
    void bulkCreateUsers_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("bulkCreateUsers", List.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreateUsers method");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    @Nested
    @DisplayName("SUCCESS Tests")
    class SuccessTests {

        // ========================================
        // SUCCESS TESTS
        // ========================================

        /**
         * Purpose: Verify bulk creation with all valid users does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - All Valid - No Exception")
        void bulkCreateUsersAsync_AllValid_Success() {
            List<UserRequestModel> users = createValidUserList(3);
            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify large batch bulk creation does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Large Batch - No Exception")
        void bulkCreateUsersAsync_LargeBatch_Success() {
            List<UserRequestModel> users = createValidUserList(10);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify bulk creation with valid list does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Multiple Users - No Exception")
        void bulkCreateUsersAsync_LogsOperation() {
            List<UserRequestModel> users = createValidUserList(2);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify single user bulk creation does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Single User - No Exception")
        void bulkCreateUsersAsync_SingleUser_Success() {
            List<UserRequestModel> users = createValidUserList(1);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }
    }

    @Nested
    @DisplayName("FAILURE Tests")
    class FailureTests {

        // ========================================
        // FAILURE TESTS
        // ========================================

        /**
         * Purpose: Verify bulk creation with duplicate emails does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Duplicate Email - No Exception")
        void bulkCreateUsersAsync_DuplicateEmail() {
            List<UserRequestModel> users = new ArrayList<>();
            users.add(createValidUserRequest("new@test.com"));
            users.add(createValidUserRequest("existing@test.com"));

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            savedUsers.put("existing@test.com", testUser);

            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify empty list does not throw unexpected exception.
         * Expected Result: No exception is thrown.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Bulk Create Users - Empty List - No exception")
        void bulkCreateUsersAsync_EmptyList() {
            assertDoesNotThrow(
                    () -> userService.bulkCreateUsersAsync(new ArrayList<>(), TEST_USER_ID, TEST_LOGIN_NAME,
                            TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify null list does not throw unexpected exception.
         * Expected Result: No exception is thrown.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Bulk Create Users - Null List - No exception")
        void bulkCreateUsersAsync_NullList() {
            assertDoesNotThrow(
                    () -> userService.bulkCreateUsersAsync(null, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify bulk creation handles partial success without throwing.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Partial Success - No Exception")
        void bulkCreateUsersAsync_PartialSuccess() {
            List<UserRequestModel> users = new ArrayList<>();
            users.add(createValidUserRequest("valid@test.com"));
            users.add(new UserRequestModel()); // Invalid - missing fields

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }
    }

    // Helper methods for bulk create tests
    private List<UserRequestModel> createValidUserList(int count) {
        List<UserRequestModel> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(createValidUserRequest("bulkuser" + i + "@test.com"));
        }
        return users;
    }

    private UserRequestModel createValidUserRequest(String email) {
        UserRequestModel user = new UserRequestModel();
        user.setLoginName(email);
        user.setFirstName("Bulk");
        user.setLastName("User");
        user.setPhone("1234567890");
        user.setRole("User");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setPermissionIds(Arrays.asList(1L));
        return user;
    }

    private void setupBulkCreateMocks() {
        lenient().when(userClientMappingRepository.save(any(UserClientMapping.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

        Client client = new Client();
        client.setClientId(TEST_CLIENT_ID);
        client.setImgbbApiKey("test-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(client));

        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(true);
    }

    private void setupPasswordHelperMocks(MockedStatic<PasswordHelper> mockedPasswordHelper) {
        mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
        mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[] { "salt123", "hashedPassword123" });
        mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
    }
}
