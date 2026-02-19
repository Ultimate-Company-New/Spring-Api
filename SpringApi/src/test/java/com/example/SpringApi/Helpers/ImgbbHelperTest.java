package com.example.SpringApi.Helpers;

import com.example.SpringApi.Helpers.ImgbbHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImgbbHelper Tests")
class ImgbbHelperTest {

    // Total Tests: 8

    /**
     * Purpose: Verify filename generators sanitize inputs and include expected suffix markers.
     * Expected Result: Generated names include environment and logical suffix tokens.
     * Assertions: Prefix/suffix and sanitized token checks.
     */
    @Test
    @DisplayName("filenameGenerators - Include Expected Tokens - Success")
    void filenameGenerators_s01_includeExpectedTokens_success() {
        // Arrange

        // Act
        String clientLogo = ImgbbHelper.generateCustomFileNameForClientLogo("dev", "Ultimate Company");
        String userProfile = ImgbbHelper.generateCustomFileNameForUserProfile("dev", "Ultimate Company", 10L);
        String productImage = ImgbbHelper.generateCustomFileNameForProductImage("prod", "ACME Inc", 11L, "main");
        String attachment = ImgbbHelper.generateCustomFileNameForPurchaseOrderAttachment("uat", "ACME Inc", 99L, "Invoice Document.pdf");

        // Assert
        assertTrue(clientLogo.startsWith("dev-Ultimate_Company-"));
        assertTrue(clientLogo.endsWith("-Logo"));
        assertTrue(userProfile.contains("dev-Ultimate_Company10_"));
        assertTrue(userProfile.endsWith("-UserProfile"));
        assertTrue(productImage.contains("prod-ACME_Inc11_"));
        assertTrue(productImage.endsWith("-main"));
        assertTrue(attachment.contains("uat-ACME_Inc99_"));
        assertTrue(attachment.endsWith("-Invoice_Document"));
    }

    /**
     * Purpose: Verify uploadFileToImgbbWithUrl returns null when delegated upload result is null.
     * Expected Result: Null URL response.
     * Assertions: Returned URL is null.
     */
    @Test
    @DisplayName("uploadFileToImgbbWithUrl - Null Upload Result Returns Null - Success")
    void uploadFileToImgbbWithUrl_s02_nullUploadResultReturnsNull_success() {
        // Arrange
        StubImgbbHelper helper = new StubImgbbHelper();
        helper.setUploadResult(null);

        // Act
        String url = helper.uploadFileToImgbbWithUrl("base64-data", "file-name");

        // Assert
        assertNull(url);
    }

    /**
     * Purpose: Verify uploadFileToImgbb returns null for null or empty image payloads.
     * Expected Result: Method exits before network attempt.
     * Assertions: Null results for null/empty payloads.
     */
    @Test
    @DisplayName("uploadFileToImgbb - Null Or Empty Payload Returns Null - Success")
    void uploadFileToImgbb_s03_nullOrEmptyPayloadReturnsNull_success() {
        // Arrange
        ImgbbHelper helper = new ImgbbHelper("api-key");

        // Act
        ImgbbHelper.ImgbbUploadResponse nullResult = helper.uploadFileToImgbb(null, "name");
        ImgbbHelper.ImgbbUploadResponse emptyResult = helper.uploadFileToImgbb("", "name");

        // Assert
        assertNull(nullResult);
        assertNull(emptyResult);
    }

    /**
     * Purpose: Verify bulk attachment upload maps notes and upload metadata in success path.
     * Expected Result: One result per attachment preserving URL/delete hash/notes.
     * Assertions: Result count and field mappings.
     */
    @Test
    @DisplayName("uploadPurchaseOrderAttachments - Success Mapping - Success")
    void uploadPurchaseOrderAttachments_s04_successMapping_success() throws IOException {
        // Arrange
        StubImgbbHelper helper = new StubImgbbHelper();
        helper.setUploadResult(new ImgbbHelper.ImgbbUploadResponse("https://img/1", "del-hash"));

        List<ImgbbHelper.AttachmentUploadRequest> requests = List.of(
                new ImgbbHelper.AttachmentUploadRequest("Invoice.pdf", "base64-1", "invoice notes")
        );

        // Act
        List<ImgbbHelper.AttachmentUploadResult> results =
                helper.uploadPurchaseOrderAttachments(requests, "dev", "Ultimate Co", 101L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("https://img/1", results.get(0).getUrl());
        assertEquals("del-hash", results.get(0).getDeleteHash());
        assertEquals("invoice notes", results.get(0).getNotes());
    }

    /**
     * Purpose: Verify bulk attachment upload throws IOException when any upload fails.
     * Expected Result: IOException with attachment name context.
     * Assertions: Exception message content.
     */
    @Test
    @DisplayName("uploadPurchaseOrderAttachments - Failed Upload Throws IOException - Success")
    void uploadPurchaseOrderAttachments_s05_failedUploadThrowsIOException_success() {
        // Arrange
        StubImgbbHelper helper = new StubImgbbHelper();
        helper.setUploadResult(null);

        List<ImgbbHelper.AttachmentUploadRequest> requests = List.of(
                new ImgbbHelper.AttachmentUploadRequest("bad-file.pdf", "base64", "notes")
        );

        // Act
        IOException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class,
                () -> helper.uploadPurchaseOrderAttachments(requests, "dev", "Ultimate Co", 101L)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Failed to upload attachment: bad-file.pdf"));
    }

    /**
     * Purpose: Verify deleteMultipleImages counts successes and continues on invalid/exceptional entries.
     * Expected Result: Only successful deletions are counted.
     * Assertions: Success count value.
     */
    @Test
    @DisplayName("deleteMultipleImages - Counts Success And Continues - Success")
    void deleteMultipleImages_s06_countsSuccessAndContinues_success() {
        // Arrange
        StubImgbbHelper helper = new StubImgbbHelper();
        helper.setDeleteResult("ok-1", true);
        helper.setDeleteResult("ok-2", true);
        helper.setDeleteResult("fail", false);
        helper.setDeleteExceptionKey("boom");

        // Act
        int deletedCount = helper.deleteMultipleImages(Arrays.asList("ok-1", "", null, "fail", "boom", "ok-2"));

        // Assert
        assertEquals(2, deletedCount);
        assertFalse(helper.deleteImage(""));
    }

    /**
     * Purpose: Verify private JSON parsing helpers and response-content reader branches via reflection.
     * Expected Result: URL and delete hash extracted correctly; null stream returns default text.
     * Assertions: Parsed values and content-reader outputs.
     */
    @Test
    @DisplayName("privateHelpers - JSON Parsing And Response Content Branches - Success")
    void privateHelpers_s07_jsonParsingAndResponseContentBranches_success() throws Exception {
        // Arrange
        ImgbbHelper helper = new ImgbbHelper("api-key");

        String json = "{\"data\":{\"url\":\"https:\\/\\/i.ibb.co\\/abc\\/img.png\",\"delete_url\":\"https://ibb.co/abc/hash123\"}}";

        Method extractUrl = ImgbbHelper.class.getDeclaredMethod("extractUrlFromJson", String.class);
        extractUrl.setAccessible(true);

        Method extractDeleteHash = ImgbbHelper.class.getDeclaredMethod("extractDeleteHashFromJson", String.class);
        extractDeleteHash.setAccessible(true);

        Method getResponseContent = ImgbbHelper.class.getDeclaredMethod("getResponseContent", java.io.InputStream.class);
        getResponseContent.setAccessible(true);

        // Act
        String url = (String) extractUrl.invoke(helper, json);
        String deleteHash = (String) extractDeleteHash.invoke(helper, json);
        String noContent = (String) getResponseContent.invoke(helper, new Object[]{null});
        String streamContent = (String) getResponseContent.invoke(
                helper,
                new ByteArrayInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8))
        );

        // Assert
        assertEquals("https://i.ibb.co/abc/img.png", url);
        assertEquals("hash123", deleteHash);
        assertEquals("No content", noContent);
        assertEquals("line1\nline2", streamContent);
    }

    /**
     * Purpose: Verify invalid patterns in private JSON parsing return null fallbacks.
     * Expected Result: No URL/delete hash parsed.
     * Assertions: Null parsing outputs.
     */
    @Test
    @DisplayName("privateHelpers - Invalid JSON Returns Null - Success")
    void privateHelpers_s08_invalidJsonReturnsNull_success() throws Exception {
        // Arrange
        ImgbbHelper helper = new ImgbbHelper("api-key");
        Method extractUrl = ImgbbHelper.class.getDeclaredMethod("extractUrlFromJson", String.class);
        extractUrl.setAccessible(true);
        Method extractDeleteHash = ImgbbHelper.class.getDeclaredMethod("extractDeleteHashFromJson", String.class);
        extractDeleteHash.setAccessible(true);

        // Act
        String url = (String) extractUrl.invoke(helper, "{}");
        String deleteHash = (String) extractDeleteHash.invoke(helper, "{\"delete_url\":\"invalid\"}");

        // Assert
        assertNull(url);
        assertNull(deleteHash);
    }

    private static final class StubImgbbHelper extends ImgbbHelper {

        private ImgbbUploadResponse uploadResult;
        private final Map<String, Boolean> deleteResults = new HashMap<>();
        private String deleteExceptionKey;

        StubImgbbHelper() {
            super("stub-api-key");
        }

        void setUploadResult(ImgbbUploadResponse uploadResult) {
            this.uploadResult = uploadResult;
        }

        void setDeleteResult(String deleteHash, boolean deleted) {
            deleteResults.put(deleteHash, deleted);
        }

        void setDeleteExceptionKey(String deleteExceptionKey) {
            this.deleteExceptionKey = deleteExceptionKey;
        }

        @Override
        public ImgbbUploadResponse uploadFileToImgbb(String imageBase64, String filePath) {
            return uploadResult;
        }

        @Override
        public boolean deleteImage(String deleteHash) {
            if (deleteHash == null || deleteHash.isEmpty()) {
                return false;
            }
            if (deleteHash.equals(deleteExceptionKey)) {
                throw new RuntimeException("forced delete failure");
            }
            return deleteResults.getOrDefault(deleteHash, false);
        }
    }
}
