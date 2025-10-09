package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for the Client table.
 * 
 * This entity represents a client/tenant in the multi-tenant system.
 * It contains all client-specific configuration including integration
 * settings for SendGrid, Razorpay, ShipRocket, and Jira.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Client`")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clientId", nullable = false)
    private Long clientId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "sendGridApiKey")
    private String sendGridApiKey;

    @Column(name = "sendGridEmailAddress")
    private String sendGridEmailAddress;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "supportEmail", nullable = false)
    private String supportEmail;

    @Column(name = "website", nullable = false)
    private String website;

    @Column(name = "sendgridSenderName", nullable = false)
    private String sendgridSenderName;

    @Column(name = "razorpayApiKey")
    private String razorpayApiKey;

    @Column(name = "razorpayApiSecret")
    private String razorpayApiSecret;

    @Column(name = "shipRocketEmail")
    private String shipRocketEmail;

    @Column(name = "shipRocketPassword")
    private String shipRocketPassword;

    @Column(name = "jiraUserName")
    private String jiraUserName;

    @Column(name = "jiraPassword")
    private String jiraPassword;

    @Column(name = "jiraProjectUrl")
    private String jiraProjectUrl;

    @Column(name = "jiraProjectKey")
    private String jiraProjectKey;

    @Column(name = "issueTypes", columnDefinition = "TEXT")
    private String issueTypes;

    @Column(name = "googleCredId")
    private Long googleCredId;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserClientMapping> userClientMappings;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGroup> userGroups;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserClientPermissionMapping> userClientPermissionMappings;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGridPreference> userGridPreferences;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "googleCredId", nullable = false, insertable = false, updatable = false)
    private GoogleCred googleCred;

    /**
     * Default constructor.
     */
    public Client() {}

    /**
     * Constructor for creating a new client.
     * 
     * @param request The ClientRequestModel containing the client data
     * @param createdUser The user creating the client
     */
    public Client(ClientRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
    }

    /**
     * Constructor for updating an existing client.
     * 
     * @param request The ClientRequestModel containing the updated client data
     * @param modifiedUser The user modifying the client
     * @param existingClient The existing client entity
     */
    public Client(ClientRequestModel request, String modifiedUser, Client existingClient) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        // Copy existing values that shouldn't change
        this.clientId = existingClient.getClientId();
        this.createdUser = existingClient.getCreatedUser();
        this.createdAt = existingClient.getCreatedAt();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;  // When updating, use the provided modified user
    }

    /**
     * Validates the request model.
     * 
     * @param request The ClientRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(ClientRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidRequest);
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidName);
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidDescription);
        }
        if (request.getSupportEmail() == null || request.getSupportEmail().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidSupportEmail);
        }
        if (request.getWebsite() == null || request.getWebsite().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidWebsite);
        }
        if (request.getSendgridSenderName() == null || request.getSendgridSenderName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName);
        }
    }

    /**
     * Validates the user parameter.
     * 
     * @param user The user to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }

    /**
     * Sets fields from the request model.
     * 
     * @param request The ClientRequestModel to extract fields from
     */
    private void setFieldsFromRequest(ClientRequestModel request) {
        this.name = request.getName().trim();
        this.description = request.getDescription().trim();
        this.sendGridApiKey = request.getSendGridApiKey() != null ? request.getSendGridApiKey().trim() : null;
        this.sendGridEmailAddress = request.getSendGridEmailAddress() != null ? request.getSendGridEmailAddress().trim() : null;
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE; // Default false if null
        this.supportEmail = request.getSupportEmail().trim();
        this.website = request.getWebsite().trim();
        this.sendgridSenderName = request.getSendgridSenderName().trim();
        this.razorpayApiKey = request.getRazorpayApiKey() != null ? request.getRazorpayApiKey().trim() : null;
        this.razorpayApiSecret = request.getRazorpayApiSecret() != null ? request.getRazorpayApiSecret().trim() : null;
        this.shipRocketEmail = request.getShipRocketEmail() != null ? request.getShipRocketEmail().trim() : null;
        this.shipRocketPassword = request.getShipRocketPassword() != null ? request.getShipRocketPassword().trim() : null;
        this.jiraUserName = request.getJiraUserName() != null ? request.getJiraUserName().trim() : null;
        this.jiraPassword = request.getJiraPassword() != null ? request.getJiraPassword().trim() : null;
        this.jiraProjectUrl = request.getJiraProjectUrl() != null ? request.getJiraProjectUrl().trim() : null;
        this.jiraProjectKey = request.getJiraProjectKey() != null ? request.getJiraProjectKey().trim() : null;
        this.issueTypes = request.getIssueTypes() != null ? request.getIssueTypes().trim() : null;
        this.googleCredId = request.getGoogleCredId();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}
