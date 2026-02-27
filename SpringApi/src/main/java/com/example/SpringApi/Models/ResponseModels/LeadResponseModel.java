package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for Lead operations.
 *
 * <p>This model contains all the fields returned when retrieving lead information. It includes
 * related entities and calculated fields for the UI.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class LeadResponseModel {

  private Long leadId;
  private String annualRevenue;
  private String company;
  private Integer companySize;
  private String email;
  private String firstName;
  private String fax;
  private String lastName;
  private String leadStatus;
  private String phone;
  private String title;
  private String website;
  private Boolean isDeleted;
  private Long clientId;
  private Long addressId;
  private Long createdById;
  private Long assignedAgentId;
  private LocalDateTime createdAt;
  private String createdUser;
  private LocalDateTime updatedAt;
  private String modifiedUser;
  private String notes;

  // Related entities
  private AddressResponseModel address;
  private UserResponseModel createdByUser;
  private UserResponseModel assignedAgent;

  // Additional computed fields
  private String fullName;
  private String displayName;
  private Boolean isAssigned;
  private Boolean isActive;
  private Integer daysOld;
  private String companySizeDisplay;

  /**
   * Minimal constructor for Purchase Order responses. Only includes leadId, firstName, lastName,
   * and email.
   *
   * @param lead The Lead entity
   * @param minimal If true, only populate minimal fields (leadId, firstName, lastName, email)
   */
  public LeadResponseModel(Lead lead, boolean minimal) {
    if (lead != null && minimal) {
      this.leadId = lead.getLeadId();
      this.firstName = lead.getFirstName();
      this.lastName = lead.getLastName();
      this.email = lead.getEmail();
      // All other fields remain null/empty
    } else if (lead != null) {
      // Call full constructor logic
      populateFullLeadData(lead);
    }
  }

  /**
   * Constructor to create response model from database entity.
   *
   * @param lead The Lead entity
   */
  public LeadResponseModel(Lead lead) {
    if (lead != null) {
      populateFullLeadData(lead);
    }
  }

  /**
   * Helper method to populate all lead data fields.
   *
   * @param lead The Lead entity
   */
  private void populateFullLeadData(Lead lead) {
    this.leadId = lead.getLeadId();
    this.annualRevenue = lead.getAnnualRevenue();
    this.company = lead.getCompany();
    this.companySize = lead.getCompanySize();
    this.email = lead.getEmail();
    this.firstName = lead.getFirstName();
    this.fax = lead.getFax();
    this.lastName = lead.getLastName();
    this.leadStatus = lead.getLeadStatus();
    this.phone = lead.getPhone();
    this.title = lead.getTitle();
    this.website = lead.getWebsite();
    this.isDeleted = lead.getIsDeleted();
    this.clientId = lead.getClientId();
    this.addressId = lead.getAddressId();
    this.createdById = lead.getCreatedById();
    this.assignedAgentId = lead.getAssignedAgentId();
    this.createdAt = lead.getCreatedAt();
    this.createdUser = lead.getCreatedUser();
    this.updatedAt = lead.getUpdatedAt();
    this.modifiedUser = lead.getModifiedUser();
    this.notes = lead.getNotes();

    // Set related entities if loaded
    this.address = lead.getAddress() != null ? new AddressResponseModel(lead.getAddress()) : null;
    this.createdByUser =
        lead.getCreatedByUser() != null ? new UserResponseModel(lead.getCreatedByUser()) : null;
    this.assignedAgent =
        lead.getAssignedAgent() != null ? new UserResponseModel(lead.getAssignedAgent()) : null;

    // Compute additional fields
    this.fullName = lead.getFullName();
    this.displayName = buildDisplayName();
    getStatusColor();
    this.isAssigned = this.assignedAgentId != null;
    this.isActive = !this.isDeleted;
    this.daysOld = calculateDaysOld();
    this.companySizeDisplay = buildCompanySizeDisplay();
  }

  /**
   * Builds a display name combining name, title, and company.
   *
   * @return Display name string
   */
  private String buildDisplayName() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.fullName);

    if (this.title != null && !this.title.trim().isEmpty()) {
      sb.append(" - ").append(this.title);
    }

    if (this.company != null && !this.company.trim().isEmpty()) {
      sb.append(" (").append(this.company).append(")");
    }

    return sb.toString();
  }

  /**
   * Gets a color code based on lead status for UI display.
   *
   * @return Color code string
   */
  private String getStatusColor() {
    if (this.leadStatus == null) return "gray";

    switch (this.leadStatus) {
      case "Contacted", "Re Qualified":
        return "green";
      case "Lost Lead", "Junk Lead", "Not Qualified":
        return "red";
      case "Attempted To Contact", "Contact In Future":
        return "orange";
      case "Not Contacted":
        return "blue";
      default:
        return "gray";
    }
  }

  /**
   * Calculates the number of days since the lead was created.
   *
   * @return Number of days old
   */
  private Integer calculateDaysOld() {
    if (this.createdAt != null) {
      LocalDateTime now = LocalDateTime.now();
      return (int) java.time.Duration.between(this.createdAt, now).toDays();
    }
    return 0;
  }

  /**
   * Builds a display string for company size.
   *
   * @return Company size display string
   */
  private String buildCompanySizeDisplay() {
    if (this.companySize != null) {
      if (this.companySize < 10) {
        return "Small (1-9 employees)";
      } else if (this.companySize < 50) {
        return "Medium (10-49 employees)";
      } else if (this.companySize < 250) {
        return "Large (50-249 employees)";
      } else {
        return "Enterprise (250+ employees)";
      }
    }
    return "Unknown size";
  }
}

