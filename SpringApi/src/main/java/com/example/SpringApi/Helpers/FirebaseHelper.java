package com.example.SpringApi.Helpers;

import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class FirebaseHelper {
  public FirebaseHelper(GoogleCred googleCred) {
    try {
      if (FirebaseApp.getApps() == null || FirebaseApp.getApps().isEmpty()) {
        // Combine all necessary details into a JSON string
        ByteArrayInputStream serviceAccount =
            new ByteArrayInputStream(
                ("{"
                        + "\"type\": \""
                        + googleCred.getType()
                        + "\","
                        + "\"project_id\": \""
                        + googleCred.getProjectId()
                        + "\","
                        + "\"private_key_id\": \""
                        + googleCred.getPrivateKeyId()
                        + "\","
                        + "\"private_key\": \""
                        + googleCred.getPrivateKey()
                        + "\","
                        + "\"client_email\": \""
                        + googleCred.getClientEmail()
                        + "\","
                        + "\"client_id\": \""
                        + googleCred.getClientId()
                        + "\","
                        + "\"auth_uri\": \""
                        + googleCred.getAuthUri()
                        + "\","
                        + "\"token_uri\": \""
                        + googleCred.getTokenUri()
                        + "\","
                        + "\"auth_provider_x509_cert_url\": \""
                        + googleCred.getAuthProviderx509CertUrl()
                        + "\","
                        + "\"client_x509_cert_url\": \""
                        + googleCred.getClientx509CertUrl()
                        + "\""
                        + "}")
                    .getBytes(StandardCharsets.UTF_8));

        // Initialize FirebaseOptions with the service account credentials
        // FIXED: Used static builder() method instead of deprecated constructor
        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(ServiceAccountCredentials.fromStream(serviceAccount))
                .setStorageBucket("ultimate-company.appspot.com")
                .build();

        FirebaseApp.initializeApp(options);
      }
    } catch (Exception ignored) {
      // Firebase app may already be initialized in another thread/context.
    }
  }

  public byte[] downloadFileAsBytesFromFirebase(String filePath) {
    // Get a reference to the Firebase storage bucket
    Bucket bucket = StorageClient.getInstance().bucket();

    // Get the file (blob) from Firebase Storage
    Blob blob = bucket.get(filePath);

    // Check if the blob exists
    if (blob == null) {
      return new byte[0];
    }

    // Download the file into a byte array
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blob.downloadTo(outputStream);

    return outputStream.toByteArray();
  }

  public String downloadFileAsBase64FromFirebase(String filePath) {
    // Get a reference to the Firebase storage bucket
    Bucket bucket = StorageClient.getInstance().bucket();

    // Get the file (blob) from Firebase Storage
    Blob blob = bucket.get(filePath);

    // Check if the blob exists
    if (blob == null) {
      // Return null if the file does not exist
      return null;
    }

    // Download the file into a byte array
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blob.downloadTo(outputStream);

    // Encode the byte array to Base64
    byte[] fileBytes = outputStream.toByteArray();
    return Base64.getEncoder().encodeToString(fileBytes); // Return the Base64 string
  }

  public boolean uploadFileToFirebase(String imageBase64, String filePath) {
    try {
      // Get Firebase bucket instance
      Bucket bucket = StorageClient.getInstance().bucket();

      // Convert base64 string to byte[]
      byte[] imageData = Base64.getDecoder().decode(imageBase64);

      // Upload the byte array to the specified file path in the bucket
      bucket.create(filePath, imageData);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void deleteFile(String filePath) {
    // Get Firebase bucket instance
    Bucket bucket = StorageClient.getInstance().bucket();

    // Get the blob (file) from the bucket
    Blob blob = bucket.get(filePath);

    // Check if the blob exists
    if (blob != null) {
      // Delete the blob
      blob.delete();
    }
  }

  /**
   * Generates the Firebase storage path for a client logo.
   *
   * @param environmentName The environment name (e.g., "development", "production")
   * @param clientName The name of the client
   * @param clientId The unique identifier of the client
   * @return The file path in Firebase storage
   */
  public static String getClientLogoPath(String environmentName, String clientName, Long clientId) {
    return clientName + "-" + clientId + "/" + environmentName + "/Logo.png";
  }
}

