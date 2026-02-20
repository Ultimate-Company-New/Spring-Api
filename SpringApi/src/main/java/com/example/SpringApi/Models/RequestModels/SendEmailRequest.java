package com.example.SpringApi.Models.RequestModels;

import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Email;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailRequest {
  private List<String> toAddress;
  private int agentId;
  private String plainTextContent;
  private String htmlContent;
  private String subject;
  private LocalDateTime sendAt;
  private String batchId;
  private boolean isInvite;
  private LocalDateTime meetingDate;
  private String location;
  private List<String> attendees;
  private List<Attachments> attachments;

  /**
   * Retrieves a list of Email objects for the "To" recipients in SendGrid.
   *
   * @return A list of Email objects representing the "To" recipients.
   */
  public List<Email> getToEmailAddressesForSendGrid() {
    // Return the list of Email objects
    List<Email> emailList = new ArrayList<>();
    for (String email : toAddress) {
      emailList.add(new Email(email));
    }
    return emailList;
  }
}
