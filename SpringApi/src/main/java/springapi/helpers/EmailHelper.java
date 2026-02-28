package springapi.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collection;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.models.requestmodels.SendEmailRequest;

/** Represents the email helper component. */
public class EmailHelper implements EmailHelperContract {
  private final String fromAddress;
  private final String senderName;
  private final String sendGridApiKey;

  /** Initializes EmailHelper. */
  public EmailHelper(String fromAddress, String senderName, String sendgridApiKey) {
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
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;"
                + "PARTSTAT=NEEDS-ACTION;CN=\"%s\";RSVP=TRUE:mailto:%s%n",
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

  /** Executes send email. */
  public boolean sendEmail(SendEmailRequest request) {
    SendGrid sendGridClient = new SendGrid(sendGridApiKey);

    // form the sendgrid mail
    Mail mail = new Mail();
    Personalization personalization = new Personalization();
    personalization.addTo(new Email(request.getToAddress().getFirst()));
    mail.addPersonalization(personalization);
    mail.setFrom(new Email(fromAddress, senderName));
    mail.setSubject(request.getSubject());
    mail.addContent(new Content("text/plain", request.getPlainTextContent()));
    mail.addContent(new Content("text/html", request.getHtmlContent()));

    // Attach any files if present
    if (request.getAttachments() != null) {
      for (Attachments attachment : request.getAttachments()) {
        Attachments calendarAttachment = new Attachments();
        calendarAttachment.setFilename(attachment.getFilename());
        calendarAttachment.setContent(attachment.getContent());
        calendarAttachment.setType(attachment.getType());
        calendarAttachment.setDisposition(attachment.getDisposition());
        calendarAttachment.setContentId(
            attachment.getContentId()); // Use content ID for inline images
        mail.addAttachments(calendarAttachment);
      }
    }

    if (request.getSendAt() != null) {
      mail.setSendAt(request.getSendAt().toEpochSecond(ZoneOffset.UTC));

      // Set the batch ID for scheduled sending
      // Note: Newly generated batch IDs don't exist in SendGrid until first use,
      // so we don't validate them. SendGrid will validate when the email is sent.
      if (request.getBatchId() != null) {
        mail.setBatchId(request.getBatchId());
      }
    }

    if (request.isInvite()) {
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
      Attachments calendarAttachment = new Attachments();
      calendarAttachment.setFilename("invite.ics");
      calendarAttachment.setContent(Base64.getEncoder().encodeToString(calendarBytes));
      calendarAttachment.setType("text/calendar");
      mail.addAttachments(calendarAttachment);
    }

    try {
      Request httpRequest = new Request();
      httpRequest.setMethod(Method.POST);
      httpRequest.setEndpoint("mail/send");
      httpRequest.setBody(mail.build());
      com.sendgrid.Response sendGridResponse = sendGridClient.api(httpRequest);
      int code = sendGridResponse.getStatusCode();
      if (code >= 200 && code < 300) {
        return true;
      }
      throw new BadRequestException(
          ErrorMessages.EmailErrorMessages.ER001 + ": " + sendGridResponse.getBody());
    } catch (IOException e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER001 + ": " + e.getMessage());
    }
  }

  /**
   * Generates a batch ID using the SendGrid API.
   *
   * @return A string representing the generated batch ID.
   */
  public String generateBatchId() {
    SendGrid sendGridClient = new SendGrid(this.sendGridApiKey);

    try {
      // Create a batch ID
      // POST /mail/batch
      Request httpRequest = new Request();
      httpRequest.setMethod(Method.POST);
      httpRequest.setEndpoint("mail/batch");

      // Set Content-Type header as required by SendGrid API
      httpRequest.addHeader("Content-Type", "application/json");

      // Send a POST request to the SendGrid API to generate a batch ID
      com.sendgrid.Response sendGridResponse = sendGridClient.api(httpRequest);
      int code = sendGridResponse.getStatusCode();
      if (code >= 200 && code < 300) {
        return new ObjectMapper().readTree(sendGridResponse.getBody()).get("batch_id").asText();
      }
      throw new BadRequestException(
          ErrorMessages.EmailErrorMessages.ER002 + ": " + sendGridResponse.getBody());
    } catch (Exception e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER002 + ": " + e.getMessage());
    }
  }

  /** Checks whether it can cel email. */
  public void cancelEmail(String batchId) {
    SendGrid sendGridClient = new SendGrid(this.sendGridApiKey);

    // Define the request body for cancelling the email batch
    String body =
        """
                {
                    "batch_id": "%s",
                    "status": "cancel"
                }"""
            .formatted(batchId);

    // Send a POST request to the SendGrid API to cancel the email batch
    try {
      // Set the request method and endpoint
      Request httpRequest = new Request();
      httpRequest.setMethod(Method.POST);
      httpRequest.setEndpoint("mail/scheduled_sends");

      // Set the request headers
      httpRequest.addHeader("Authorization", "Bearer " + this.sendGridApiKey);

      // Set the request body
      httpRequest.setBody(body);

      // Send the request
      com.sendgrid.Response sendGridResponse = sendGridClient.api(httpRequest);

      // Check the response status code
      int code = sendGridResponse.getStatusCode();
      if (code >= 200 && code < 300) {
        return;
      }
      throw new BadRequestException(
          ErrorMessages.EmailErrorMessages.ER003 + ": " + sendGridResponse.getBody());
    } catch (Exception e) {
      throw new BadRequestException(ErrorMessages.EmailErrorMessages.ER003 + ": " + e.getMessage());
    }
  }
}
