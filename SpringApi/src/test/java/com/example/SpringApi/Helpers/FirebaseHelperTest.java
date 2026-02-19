package com.example.SpringApi.Helpers;

import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FirebaseHelper Tests")
class FirebaseHelperTest {

    // Total Tests: 10

    /**
     * Purpose: Verify constructor safely skips initialization when a Firebase app already exists.
     * Expected Result: No exception and initializeApp is not called.
     * Assertions: No exception and static initialization skip verification.
     */
    @Test
    @DisplayName("constructor - Existing App Skips Initialization - Success")
    void constructor_s01_existingAppSkipsInitialization_success() {
        // Arrange
        GoogleCred googleCred = buildGoogleCred();

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

            // Act + Assert
            assertDoesNotThrow(() -> new FirebaseHelper(googleCred));
            firebaseAppMock.verify(() -> FirebaseApp.initializeApp(any(com.google.firebase.FirebaseOptions.class)), never());
        }
    }

    /**
     * Purpose: Verify constructor catches initialization errors when apps are empty.
     * Expected Result: No exception escapes constructor.
     * Assertions: Constructor does not throw.
     */
    @Test
    @DisplayName("constructor - Empty Apps Catches Initialization Error - Success")
    void constructor_s02_emptyAppsCatchesInitializationError_success() {
        // Arrange
        GoogleCred googleCred = buildGoogleCred();
        googleCred.setPrivateKey("invalid-key-material");

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());

            // Act + Assert
            assertDoesNotThrow(() -> new FirebaseHelper(googleCred));
        }
    }

    /**
     * Purpose: Verify downloadFileAsBytesFromFirebase returns empty array when blob is absent.
     * Expected Result: Empty byte array.
     * Assertions: Zero-length array check.
     */
    @Test
    @DisplayName("downloadFileAsBytesFromFirebase - Missing Blob Returns Empty - Success")
    void downloadFileAsBytesFromFirebase_s03_missingBlobReturnsEmpty_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("missing/path")).thenReturn(null);

            // Act
            byte[] bytes = helper.downloadFileAsBytesFromFirebase("missing/path");

            // Assert
            assertEquals(0, bytes.length);
        }
    }

    /**
     * Purpose: Verify downloadFileAsBytesFromFirebase returns downloaded bytes when blob exists.
     * Expected Result: Byte payload from blob output stream.
     * Assertions: Downloaded byte content equality.
     */
    @Test
    @DisplayName("downloadFileAsBytesFromFirebase - Existing Blob Returns Bytes - Success")
    void downloadFileAsBytesFromFirebase_s04_existingBlobReturnsBytes_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);
        Blob blob = mock(Blob.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("file/path")).thenReturn(blob);
            doAnswer(invocation -> {
                java.io.OutputStream outputStream = invocation.getArgument(0);
                outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
                return null;
            }).when(blob).downloadTo(any(java.io.OutputStream.class));

            // Act
            byte[] bytes = helper.downloadFileAsBytesFromFirebase("file/path");

            // Assert
            assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), bytes);
        }
    }

    /**
     * Purpose: Verify downloadFileAsBase64FromFirebase returns null when blob does not exist.
     * Expected Result: Null return.
     * Assertions: Null check.
     */
    @Test
    @DisplayName("downloadFileAsBase64FromFirebase - Missing Blob Returns Null - Success")
    void downloadFileAsBase64FromFirebase_s05_missingBlobReturnsNull_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("missing/path")).thenReturn(null);

            // Act
            String base64 = helper.downloadFileAsBase64FromFirebase("missing/path");

            // Assert
            assertNull(base64);
        }
    }

    /**
     * Purpose: Verify downloadFileAsBase64FromFirebase encodes downloaded bytes properly.
     * Expected Result: Base64 string of file bytes.
     * Assertions: Encoded value equality.
     */
    @Test
    @DisplayName("downloadFileAsBase64FromFirebase - Existing Blob Returns Base64 - Success")
    void downloadFileAsBase64FromFirebase_s06_existingBlobReturnsBase64_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);
        Blob blob = mock(Blob.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("file/path")).thenReturn(blob);
            doAnswer(invocation -> {
                java.io.OutputStream outputStream = invocation.getArgument(0);
                outputStream.write("bytes".getBytes(StandardCharsets.UTF_8));
                return null;
            }).when(blob).downloadTo(any(java.io.OutputStream.class));

            // Act
            String base64 = helper.downloadFileAsBase64FromFirebase("file/path");

            // Assert
            assertEquals(Base64.getEncoder().encodeToString("bytes".getBytes(StandardCharsets.UTF_8)), base64);
        }
    }

    /**
     * Purpose: Verify uploadFileToFirebase returns true for valid Base64 and successful bucket create.
     * Expected Result: True return.
     * Assertions: True result and bucket create invocation.
     */
    @Test
    @DisplayName("uploadFileToFirebase - Valid Input Returns True - Success")
    void uploadFileToFirebase_s07_validInputReturnsTrue_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());
        String base64Data = Base64.getEncoder().encodeToString("image-data".getBytes(StandardCharsets.UTF_8));

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.create(anyString(), any(byte[].class))).thenReturn(mock(Blob.class));

            // Act
            boolean uploaded = helper.uploadFileToFirebase(base64Data, "images/path.png");

            // Assert
            assertTrue(uploaded);
            verify(bucket).create(anyString(), any(byte[].class));
        }
    }

    /**
     * Purpose: Verify uploadFileToFirebase returns false when Base64 decoding or upload fails.
     * Expected Result: False return.
     * Assertions: False result.
     */
    @Test
    @DisplayName("uploadFileToFirebase - Invalid Base64 Returns False - Success")
    void uploadFileToFirebase_s08_invalidBase64ReturnsFalse_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        // Act
        boolean uploaded = helper.uploadFileToFirebase("not-valid-base64", "images/path.png");

        // Assert
        assertFalse(uploaded);
    }

    /**
     * Purpose: Verify deleteFile removes blob when it exists.
     * Expected Result: blob.delete invoked.
     * Assertions: delete invocation verification.
     */
    @Test
    @DisplayName("deleteFile - Existing Blob Is Deleted - Success")
    void deleteFile_s09_existingBlobIsDeleted_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);
        Blob blob = mock(Blob.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("file/to/delete")).thenReturn(blob);

            // Act
            helper.deleteFile("file/to/delete");

            // Assert
            verify(blob).delete();
        }
    }

    /**
     * Purpose: Verify deleteFile and client-logo-path helper cover null-blob and path formatting branches.
     * Expected Result: No delete call for null blob and expected path format.
     * Assertions: No delete invocation and path equality.
     */
    @Test
    @DisplayName("deleteFileAndPath - Null Blob Skip Delete And Path Format - Success")
    void deleteFileAndPath_s10_nullBlobSkipDeleteAndPathFormat_success() {
        // Arrange
        FirebaseHelper helper = new FirebaseHelper(buildGoogleCred());

        StorageClient storageClient = mock(StorageClient.class);
        Bucket bucket = mock(Bucket.class);

        try (MockedStatic<StorageClient> storageClientMock = mockStatic(StorageClient.class)) {
            storageClientMock.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(bucket);
            when(bucket.get("missing/file")).thenReturn(null);

            // Act
            helper.deleteFile("missing/file");
            String logoPath = FirebaseHelper.getClientLogoPath("dev", "UltimateCo", 10L);

            // Assert
            verify(bucket).get("missing/file");
            assertEquals("UltimateCo-10/dev/Logo.png", logoPath);
        }
    }

    private static GoogleCred buildGoogleCred() {
        GoogleCred cred = new GoogleCred();
        cred.setType("service_account");
        cred.setProjectId("project-id");
        cred.setPrivateKeyId("private-key-id");
        cred.setPrivateKey("-----BEGIN PRIVATE KEY-----\\nINVALID\\n-----END PRIVATE KEY-----\\n");
        cred.setClientEmail("client@example.com");
        cred.setClientId("123456789");
        cred.setAuthUri("https://accounts.google.com/o/oauth2/auth");
        cred.setTokenUri("https://oauth2.googleapis.com/token");
        cred.setAuthProviderx509CertUrl("https://www.googleapis.com/oauth2/v1/certs");
        cred.setClientx509CertUrl("https://www.googleapis.com/robot/v1/metadata/x509/client%40example.com");
        return cred;
    }
}
