package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Permission;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response model for Permission operations.
 * 
 * This model contains all the fields returned when retrieving permission information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-13
 */
@Getter
@Setter
public class PermissionResponseModel {
    
    private Long permissionId;
    private String permissionName;
    private String permissionCode;
    private String description;
    private String category;
    private Boolean isDeleted;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    /**
     * Default constructor.
     */
    public PermissionResponseModel() {}
    
    /**
     * Constructor that populates fields from a Permission entity.
     * 
     * @param permission The Permission entity to populate from
     */
    public PermissionResponseModel(Permission permission) {
        if (permission != null) {
            this.permissionId = permission.getPermissionId();
            this.permissionName = permission.getPermissionName();
            this.permissionCode = permission.getPermissionCode();
            this.description = permission.getDescription();
            this.category = permission.getCategory();
            this.isDeleted = permission.getIsDeleted();
            this.createdUser = permission.getCreatedUser();
            this.modifiedUser = permission.getModifiedUser();
            this.createdAt = permission.getCreatedAt();
            this.updatedAt = permission.getUpdatedAt();
            this.notes = permission.getNotes();
        }
    }
}

