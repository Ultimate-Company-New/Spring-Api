package com.example.SpringApi.Helpers;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.SendEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.helpers.mail.objects.Attachments;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrevoEmailHelper implements IEmailHelper {
  private static final String BREVO_API_BASE = "https://api.brevo.com/v3";
  private static final String APPLICATION_JSON = "application/json";
  private static final DateTimeFormatter ISO_UTC_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final String fromAddress;
  private final String senderName;
  private final String sendGridApiKey; // Stores Brevo API key (passed from DB as sendgridApiKey)

  public BrevoEmailHelper(String fromAddress, String senderName, String sendgridApiKey) {
    this.fromAddress = fromAddress;
    this.senderName = senderName;
    this.sendGridApiKey = sendgridApiKey;
  }

  /**
   * Generates a meeting request string in iCalendar format.
   *
   * @param from The email address of the meeting organizer.
   * @param toUsers A collection of email addresses of the meeting attendees.
   * @param subject The subject of the meeting request.
   * @param desc The description of the meeting request.
   * @param startTime The start time of the meeting.
   * @param endTime The end time of the meeting.
   * @param isCancel Specifies whether the meeting request is a cancellation request. Default is
   *     false.
   * @return A string representing the meeting request in iCalendar format.
   */
  private String meetingRequestString(
      String from,
      Collection<String> toUsers,
      String subject,
      String desc,
      LocalDateTime startTime,
      LocalDateTime endTime,
      boolean isCancel) {
    String lineSeparator = System.lineSeparator();
    String htmlDescription = desc.replaceAll("\\R", "<br>");
    return "BEGIN:VCALENDAR"
        + lineSeparator
        + "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN"
        + lineSeparator
        + "VERSION:2.0"
        + lineSeparator
        + String.format("METHOD:%s%n", isCancel ? "CANCEL" : "REQUEST")
        + "BEGIN:VEVENT"
        + lineSeparator
        + String.format(
            "DTSTART:%s%n", startTime.atZone(ZoneId.systemDefault()).toInstant().toString())
        + String.format(
            "DTSTAMP:%s%n",
            LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toString())
        + String.format("DTEND:%s%n", endTime.atZone(ZoneId.systemDefault()).toInstant().toString())
        + String.format("DESCRIPTION:%s%n", htmlDescription)
        + String.format("X-ALT-DESC;FMTTYPE=text/html:%s%n", htmlDescription)
        + String.format("SUMMARY:%s%n", subject)
        + String.format("ORGANIZER;CN=\"%s\":MAILTO:%s%n", from, from)
        + String.format(
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;CN=\"%s\";RSVP=TRUE:mailto:%s%n",
            String.join(",", toUsers), String.join(",", toUsers))
        + "BEGIN:VALARM"
        + lineSeparator
        + "TRIGGER:-PT15M"
        + lineSeparator
        + "ACTION:DISPLAY"
        + lineSeparator
        + "DESCRIPTION:Reminder"
        + lineSeparator
        + "END:VALARM"
        + lineSeparator
        + "END:VEVENT"
        + lineSeparator
        + "END:VCALENDAR"
        + lineSeparator;
  }

  public boolean sendEmail(SendEmailRequest request) {
    try {
      Map<String, Object> body = new java.util.HashMap<>();
      body.put(
          "sender", Map.of("email", fromAddress, "name", senderName != null ? senderName : ""));
      body.put("to", List.of(Map.of("email", request.getToAddress().getFirst())));
      body.put("subject", request.getSubject() != null ? request.getSubject() : "");
      body.put("htmlContent", request.getHtmlContent() != null ? request.getHtmlContent() : "");
      body.put(
          "textContent",
          request.getPlainTextContent() != null ? request.getPlainTextContent() : "");

      List<Map<String, String>> brevoAttachments = new java.util.ArrayList<>();
      // Attach any files if present
      if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
        for (Attachments attachment : request.getAttachments()) {
          brevoAttachments.add(
              Map.of(
                  "name",
                      attachment.getFilename() != null ? attachment.getFilename() : "attachment",
                  "content", attachment.getContent() != null ? attachment.getContent() : ""));
        }
      }

      if (request.getSendAt() != null) {
        String scheduledAt = request.getSendAt().atZone(ZoneOffset.UTC).format(ISO_UTC_FORMATTER);
        body.put("scheduledAt", scheduledAt);
        if (request.getBatchId() != null) {
          body.put("batchId", request.getBatchId());
        }
      }

      if (request.isInvite() && request.getAttendees() != null) {
        String calendarContent =
            meetingRequestString(
                senderName,
                request.getAttendees(),
                request.getSubject(),
                request.getPlainTextContent(),
                request.getMeetingDate(),
                request.getMeetingDate().plusHours(1),
                false);

        byte[] calendarBytes = calendarContent.getBytes(StandardCharsets.UTF_8);
        String base64Calendar = Base64.getEncoder().encodeToString(calendarBytes);

        brevoAttachments.add(Map.of("name", "invite.ics", "content", base64Calendar));
      }

      if (!brevoAttachments.isEmpty()) {
        body.put("attachment", brevoAttachments);
      }

      String jsonBody = new ObjectMapper().writeValueAsString(body);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(BREVO_API_BASE + "/smtp/email"))
              .header("api-key", sendGridApiKey)
              .header("Content-Type", APPLICATION_JSON)
              .header("accept", APPLICATION_JSON)
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response =
          client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int code = response.statusCode();

      if (code >= 200 && code < 300) {
        return true;
      }
      throw new BadRequestException(
          ErrorMessages.EmailErrorMessages.ER001 + ": " + response.body());
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER001 + ": " + e.getMessage());
    }
  }

  /**
   * Generates a batch ID for Brevo scheduled batch sends. Brevo recommends using UUIDv4 for batch
   * identification (client-generated).
   *
   * @return A string representing the generated batch ID (UUID).
   */
  public String generateBatchId() {
    try {
      return UUID.randomUUID().toString();
    } catch (Exception e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER002 + ": " + e.getMessage());
    }
  }

  /**
   * Cancels a scheduled email batch using the Brevo API.
   *
   * @param batchId The ID of the email batch to cancel.
   */
  public void cancelEmail(String batchId) {
    try {
      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(BREVO_API_BASE + "/smtp/email/" + batchId))
              .header("api-key", sendGridApiKey)
              .header("accept", APPLICATION_JSON)
              .DELETE()
              .build();

      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response =
          client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int code = response.statusCode();

      if (code >= 200 && code < 300) {
        return;
      }
      throw new BadRequestException(
          ErrorMessages.EmailErrorMessages.ER003 + ": " + response.body());
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER003 + ": " + e.getMessage());
    }
  }
}

