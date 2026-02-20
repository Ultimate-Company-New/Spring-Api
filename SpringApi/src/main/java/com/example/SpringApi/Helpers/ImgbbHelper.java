package com.example.SpringApi.Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class ImgbbHelper {
  // --- Configuration ---
  private final String imgbbApiKey;
  private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";
  private static final String IMGBB_INFO_URL = "https://api.imgbb.com/1/image/";
  private static final String FILE_NAME_TIMESTAMP_PATTERN = "MM_dd_yyyy_hh_mm_ss_a";
  private static final String API_KEY_QUERY_PARAM = "?key=";

  /**
   * Constructor to initialize ImgbbHelper with API key.
   *
   * @param imgbbApiKey The ImgBB API key from client configuration
   */
  public ImgbbHelper(String imgbbApiKey) {
    this.imgbbApiKey = imgbbApiKey;
  }

  /**
   * Generates a custom filename for client logo ImgBB uploads. Format:
   * <environment>-<clientName>-<date>-Logo Note: Extension is NOT included as ImgBB adds it
   * automatically based on image type
   *
   * @param environment The environment name (e.g., "localhost", "production")
   * @param clientName The client name (spaces replaced with underscores)
   * @return The formatted filename (without extension)
   */
  public static String generateCustomFileNameForClientLogo(String environment, String clientName) {
    // Replace spaces with underscores in client name
    String sanitizedClientName = clientName.replaceAll("\\s+", "_");

    // Format date as MM_dd_yyyy_HH_mm_ss
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
    String formattedDate = dateFormat.format(new Date());

    // Construct filename (no extension - ImgBB adds it automatically)
    return environment + "-" + sanitizedClientName + "-" + formattedDate + "-Logo";
  }

  /**
   * Generates a custom filename for user profile picture ImgBB uploads. Format:
   * <environment>-<clientName><userId>_<timestamp>-UserProfile Example:
   * localhost-My_Company_123_11_01_2024_03_45_30_PM-UserProfile Note: Extension is NOT included as
   * ImgBB adds it automatically based on image type
   *
   * @param environment The environment name (e.g., "localhost", "production")
   * @param clientName The client name (spaces replaced with underscores)
   * @param userId The user ID
   * @return The formatted filename (without extension)
   */
  public static String generateCustomFileNameForUserProfile(
      String environment, String clientName, Long userId) {
    // Replace spaces with underscores in client name
    String sanitizedClientName = clientName.replaceAll("\\s+", "_");

    // Format timestamp as MM_dd_yyyy_hh_mm_ss_a (with AM/PM)
    SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_TIMESTAMP_PATTERN);
    String timestamp = dateFormat.format(new Date());

    // Construct filename (no extension - ImgBB adds it automatically)
    return environment + "-" + sanitizedClientName + userId + "_" + timestamp + "-UserProfile";
  }

  /**
   * Generates a custom filename for product image ImgBB uploads. Format:
   * <environment>-<clientName><productId>_<timestamp>-<imageName> Example:
   * localhost-My_Company_456_11_01_2024_03_45_30_PM-main Note: Extension is NOT included as ImgBB
   * adds it automatically based on image type
   *
   * @param environment The environment name (e.g., "localhost", "production")
   * @param clientName The client name (spaces replaced with underscores)
   * @param productId The product ID
   * @param imageName The image name - use constants from {@link
   *     com.example.SpringApi.Constants.ProductImageConstants}
   * @return The formatted filename (without extension)
   */
  public static String generateCustomFileNameForProductImage(
      String environment, String clientName, Long productId, String imageName) {
    // Replace spaces with underscores in client name
    String sanitizedClientName = clientName.replaceAll("\\s+", "_");

    // Format timestamp as MM_dd_yyyy_hh_mm_ss_a (with AM/PM)
    SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_TIMESTAMP_PATTERN);
    String timestamp = dateFormat.format(new Date());

    // Construct filename (no extension - ImgBB adds it automatically)
    return environment + "-" + sanitizedClientName + productId + "_" + timestamp + "-" + imageName;
  }

  /**
   * Generates a custom filename for purchase order attachment ImgBB uploads. Format:
   * <environment>-<clientName><purchaseOrderId>_<timestamp>-<attachmentName> Example:
   * localhost-My_Company_789_11_01_2024_03_45_30_PM-Invoice_Document Note: Extension is NOT
   * included as ImgBB adds it automatically based on file type
   *
   * @param environment The environment name (e.g., "localhost", "production")
   * @param clientName The client name (spaces replaced with underscores)
   * @param purchaseOrderId The purchase order ID
   * @param attachmentName The attachment name (from fileName field, sanitized)
   * @return The formatted filename (without extension)
   */
  public static String generateCustomFileNameForPurchaseOrderAttachment(
      String environment, String clientName, Long purchaseOrderId, String attachmentName) {
    // Replace spaces with underscores in client name
    String sanitizedClientName = clientName.replaceAll("\\s+", "_");

    // Sanitize attachment name (remove extension and special characters)
    String sanitizedAttachmentName = attachmentName;
    if (attachmentName.contains(".")) {
      sanitizedAttachmentName = attachmentName.substring(0, attachmentName.lastIndexOf("."));
    }
    sanitizedAttachmentName = sanitizedAttachmentName.replaceAll("[^a-zA-Z0-9_-]", "_");

    // Format timestamp as MM_dd_yyyy_hh_mm_ss_a (with AM/PM)
    SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_TIMESTAMP_PATTERN);
    String timestamp = dateFormat.format(new Date());

    // Construct filename (no extension - ImgBB adds it automatically)
    return environment
        + "-"
        + sanitizedClientName
        + purchaseOrderId
        + "_"
        + timestamp
        + "-"
        + sanitizedAttachmentName;
  }

  /** Response object containing both URL and delete hash from ImgBB upload. */
  public static class ImgbbUploadResponse {
    private final String url;
    private final String deleteHash;

    public ImgbbUploadResponse(String url, String deleteHash) {
      this.url = url;
      this.deleteHash = deleteHash;
    }

    public String getUrl() {
      return url;
    }

    public String getDeleteHash() {
      return deleteHash;
    }
  }

  /**
   * Uploads a Base64 encoded image directly to ImgBB and returns the URL.
   *
   * @param imageBase64 The Base64 encoded image string.
   * @param filePath The desired filename/identifier (used as the name parameter in ImgBB).
   * @return The public URL of the uploaded image, or null if the upload fails.
   */
  public String uploadFileToImgbbWithUrl(String imageBase64, String filePath) {
    ImgbbUploadResponse response = uploadFileToImgbb(imageBase64, filePath);
    return response != null ? response.getUrl() : null;
  }

  /**
   * Uploads a Base64 encoded image to ImgBB and returns both URL and delete hash.
   *
   * @param imageBase64 The Base64 encoded image string.
   * @param filePath The desired filename/identifier (used as the name parameter in ImgBB).
   * @return ImgbbUploadResponse containing URL and deleteHash, or null if the upload fails.
   */
  public ImgbbUploadResponse uploadFileToImgbb(String imageBase64, String filePath) {
    if (imageBase64 == null || imageBase64.isEmpty()) {
      return null;
    }

    try {
      // Build the POST request payload with name parameter
      String postParameters = "image=" + URLEncoder.encode(imageBase64, StandardCharsets.UTF_8);

      // Add name parameter if filePath is provided
      if (filePath != null && !filePath.isEmpty()) {
        postParameters += "&name=" + URLEncoder.encode(filePath, StandardCharsets.UTF_8);
      }

      // Construct the full API URL including the key
      String fullUrlString = IMGBB_UPLOAD_URL + API_KEY_QUERY_PARAM + imgbbApiKey;
      URL url = java.net.URI.create(fullUrlString).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Configure the connection for POST request
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
      connection.setRequestProperty("Content-Length", String.valueOf(postParameters.length()));

      // Send the request
      try (OutputStream os = connection.getOutputStream()) {
        os.write(postParameters.getBytes(StandardCharsets.UTF_8));
        os.flush();
      }

      // Read the response
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        String jsonResponse = getResponseContent(connection.getInputStream());
        String imageUrl = extractUrlFromJson(jsonResponse);
        String deleteHash = extractDeleteHashFromJson(jsonResponse);
        return new ImgbbUploadResponse(imageUrl, deleteHash);
      } else {
        // Error response - return null
        getResponseContent(connection.getErrorStream());
        return null;
      }

    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Deletes an image from ImgBB using the delete hash.
   *
   * @param deleteHash The delete hash obtained during upload
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteImage(String deleteHash) {
    if (deleteHash == null || deleteHash.isEmpty()) {
      return false;
    }

    try {
      String deleteUrl = IMGBB_INFO_URL + deleteHash + API_KEY_QUERY_PARAM + imgbbApiKey;
      URL url = java.net.URI.create(deleteUrl).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("DELETE");
      connection.setRequestProperty("Accept", "application/json");

      int responseCode = connection.getResponseCode();
      return responseCode == HttpURLConnection.HTTP_OK;

    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Attempts to download the file contents as a raw byte array from the public URL. NOTE: ImgBB
   * does not offer a direct API endpoint to get the image contents, so we download directly from
   * the public URL.
   *
   * @param publicUrl The public URL of the image hosted on ImgBB.
   * @return The image data as a byte array, or null on failure.
   */
  public byte[] downloadFileAsBytesFromImgBB(String publicUrl) {
    try {
      URL url = java.net.URI.create(publicUrl).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = connection.getInputStream()) {
          // Read all bytes from the input stream
          return inputStream.readAllBytes();
        }
      } else {
        return new byte[0];
      }
    } catch (IOException e) {
      return new byte[0];
    }
  }

  /**
   * Checks if a file exists on ImgBB by checking the image info endpoint. This requires the ImgBB
   * image ID, which must be extracted from the public URL or stored during the upload.
   *
   * @param imageId The ImgBB image ID (e.g., "2ndCYJK" from ibb.co/2ndCYJK).
   * @return true if the file exists and is accessible, false otherwise.
   */
  public boolean fileExists(String imageId) {
    String fullUrlString = IMGBB_INFO_URL + imageId + API_KEY_QUERY_PARAM + imgbbApiKey;

    try {
      URL url = java.net.URI.create(fullUrlString).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();

      int responseCode = connection.getResponseCode();

      // ImgBB returns 200 (HTTP_OK) for success and usually 404/400 for not found/invalid ID
      return responseCode == HttpURLConnection.HTTP_OK;
    } catch (IOException e) {
      return false;
    }
  }

  /** Helper method to read the content of an InputStream into a String. */
  private String getResponseContent(InputStream inputStream) throws IOException {
    if (inputStream == null) return "No content";
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  /**
   * Simple (and brittle) method to extract the display URL from the ImgBB JSON response. A robust
   * solution should use a JSON parsing library (like Gson or Jackson).
   */
  private String extractUrlFromJson(String json) {
    // Look for "url" (this is the key ImgBB uses for the public URL)
    String searchKey = "\"url\":\"";
    int startIndex = json.indexOf(searchKey);

    if (startIndex != -1) {
      startIndex += searchKey.length();
      int endIndex = json.indexOf("\"", startIndex);
      if (endIndex != -1) {
        return json.substring(startIndex, endIndex).replace("\\/", "/");
      }
    }
    return null; // URL not found
  }

  /**
   * Extracts the delete hash from the ImgBB JSON response. The delete hash is used to delete the
   * image later.
   */
  private String extractDeleteHashFromJson(String json) {
    // Look for "delete_url" which contains the deletehash
    String searchKey = "\"delete_url\":\"";
    int startIndex = json.indexOf(searchKey);

    if (startIndex != -1) {
      startIndex += searchKey.length();
      int endIndex = json.indexOf("\"", startIndex);
      if (endIndex != -1) {
        String deleteUrl = json.substring(startIndex, endIndex);
        // Extract the hash from the URL (format: https://ibb.co/abc123/deletehash)
        // The deletehash is the last segment of the URL
        int lastSlash = deleteUrl.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < deleteUrl.length() - 1) {
          return deleteUrl.substring(lastSlash + 1);
        }
      }
    }
    return null; // Delete hash not found
  }

  /** Represents an attachment to be uploaded with its metadata. */
  public static class AttachmentUploadRequest {
    private final String fileName;
    private final String base64Data;
    private final String notes;

    public AttachmentUploadRequest(String fileName, String base64Data, String notes) {
      this.fileName = fileName;
      this.base64Data = base64Data;
      this.notes = notes;
    }

    public String getFileName() {
      return fileName;
    }

    public String getBase64Data() {
      return base64Data;
    }

    public String getNotes() {
      return notes;
    }
  }

  /** Represents the result of an uploaded attachment with URL, delete hash, and notes. */
  public static class AttachmentUploadResult {
    private final String url;
    private final String deleteHash;
    private final String notes;

    public AttachmentUploadResult(String url, String deleteHash, String notes) {
      this.url = url;
      this.deleteHash = deleteHash;
      this.notes = notes;
    }

    public String getUrl() {
      return url;
    }

    public String getDeleteHash() {
      return deleteHash;
    }

    public String getNotes() {
      return notes;
    }
  }

  /**
   * Uploads multiple purchase order attachments to ImgBB in bulk.
   *
   * @param attachments List of attachments to upload
   * @param environment The environment name (e.g., "localhost", "production")
   * @param clientName The client name
   * @param purchaseOrderId The purchase order ID
   * @return List of upload results containing URL, delete hash, and notes for each attachment
   * @throws IOException if upload fails
   */
  public java.util.List<AttachmentUploadResult> uploadPurchaseOrderAttachments(
      java.util.List<AttachmentUploadRequest> attachments,
      String environment,
      String clientName,
      Long purchaseOrderId)
      throws IOException {

    java.util.List<AttachmentUploadResult> results = new java.util.ArrayList<>();

    for (AttachmentUploadRequest attachment : attachments) {
      // Generate custom filename for ImgBB
      String customFileName =
          generateCustomFileNameForPurchaseOrderAttachment(
              environment, clientName, purchaseOrderId, attachment.getFileName());

      // Upload to ImgBB with custom filename
      ImgbbUploadResponse uploadResult =
          uploadFileToImgbb(attachment.getBase64Data(), customFileName);

      if (uploadResult != null && uploadResult.getUrl() != null) {
        results.add(
            new AttachmentUploadResult(
                uploadResult.getUrl(), uploadResult.getDeleteHash(), attachment.getNotes()));
      } else {
        throw new IOException("Failed to upload attachment: " + attachment.getFileName());
      }
    }

    return results;
  }

  /**
   * Deletes multiple images from ImgBB using their delete hashes. Continues even if some deletions
   * fail.
   *
   * @param deleteHashes List of delete hashes to remove from ImgBB
   * @return Number of successfully deleted images
   */
  public int deleteMultipleImages(java.util.List<String> deleteHashes) {
    int successCount = 0;

    for (String deleteHash : deleteHashes) {
      if (deleteHash != null && !deleteHash.trim().isEmpty()) {
        try {
          boolean deleted = deleteImage(deleteHash);
          if (deleted) {
            successCount++;
          }
        } catch (Exception e) {
          // Error deleting image - continue with other deletions
        }
      }
    }

    return successCount;
  }
}
