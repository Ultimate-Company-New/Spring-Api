package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.ProductCategoryRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for the ProductCategory table.
 * 
 * This entity represents hierarchical product category structure.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ProductCategory")
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryId", nullable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parentId")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId", insertable = false, updatable = false)
    private ProductCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @Column(name = "isEnd", nullable = false)
    private Boolean isEnd;

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

    /**
     * Default constructor.
     */
    public ProductCategory() {}

    /**
     * Constructor for creating a new product category.
     * 
     * @param request The ProductCategoryRequestModel containing the category data
     * @param createdUser The user creating the category
     */
    public ProductCategory(ProductCategoryRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }

    /**
     * Constructor for updating an existing product category.
     * 
     * @param request The ProductCategoryRequestModel containing the updated category data
     * @param modifiedUser The user modifying the category
     * @param existingCategory The existing category entity
     */
    public ProductCategory(ProductCategoryRequestModel request, String modifiedUser, ProductCategory existingCategory) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        this.categoryId = existingCategory.getCategoryId();
        this.createdUser = existingCategory.getCreatedUser();
        this.createdAt = existingCategory.getCreatedAt();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;
    }

    /**
     * Validates the request model.
     * 
     * @param request The ProductCategoryRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(ProductCategoryRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.ProductCategoryErrorMessages.INVALID_REQUEST);
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ProductCategoryErrorMessages.INVALID_NAME);
        }
        if (request.getIsEnd() == null) {
            throw new BadRequestException(ErrorMessages.ProductCategoryErrorMessages.INVALID_IS_END);
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
            throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
        }
    }

    /**
     * Sets fields from the request model.
     * 
     * @param request The ProductCategoryRequestModel to extract fields from
     */
    private void setFieldsFromRequest(ProductCategoryRequestModel request) {
        this.name = request.getName().trim();
        this.parentId = request.getParentId();
        this.isEnd = request.getIsEnd();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}
