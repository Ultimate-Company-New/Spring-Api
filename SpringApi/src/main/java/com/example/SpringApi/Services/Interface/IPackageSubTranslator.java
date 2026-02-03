package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;

import java.util.List;

/**
 * Interface for Package operations and business logic.
 * Provides methods for managing packages including CRUD operations,
 * batch processing, and specialized queries.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IPackageSubTranslator {

    /**
     * Retrieves packages in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     *
     * @param paginationBaseRequestModel The request model containing pagination and filter parameters
     * @return PaginationBaseResponseModel containing paginated package data
     */
    PaginationBaseResponseModel<PackageResponseModel> getPackagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel);

    /**
     * Retrieves detailed information for a specific package by ID.
     *
     * @param packageId The unique identifier of the package
     * @return PackageResponseModel with complete package details
     */
    PackageResponseModel getPackageById(Long packageId);

    /**
     * Toggles the active status of a package.
     *
     * @param packageId The unique identifier of the package to toggle
     */
    void togglePackage(Long packageId);

    /**
     * Updates an existing package with new information.
     *
     * @param packageRequest The updated package data
     */
    void updatePackage(PackageRequestModel packageRequest);

    /**
     * Retrieves all packages available at a specific pickup location.
     *
     * @param pickupLocationId The unique identifier of the pickup location
     * @return List of PackageResponseModel entities available at the pickup location
     */
    List<PackageResponseModel> getPackagesByPickupLocationId(Long pickupLocationId);

    /**
     * Creates a new package in the system.
     *
     * @param packageRequest The package data to create
     */
    void createPackage(PackageRequestModel packageRequest);
    
    /**
     * Creates multiple packages asynchronously in the system with partial success support.
     * 
     * This method processes packages in a background thread with the following characteristics:
     * - Supports partial success: if some packages fail validation, others still succeed
     * - Sends detailed results to user via message notification after processing completes
     * 
     * @param packages List of PackageRequestModel containing the package data to create
     * @param requestingUserId The ID of the user making the request (captured from security context)
     * @param requestingUserLoginName The loginName of the user making the request (captured from security context)
     * @param requestingClientId The client ID of the user making the request (captured from security context)
     */
    void bulkCreatePackagesAsync(java.util.List<PackageRequestModel> packages, Long requestingUserId, String requestingUserLoginName, Long requestingClientId);

    /**
     * Creates multiple packages synchronously in a single operation (for testing).
     * This is a synchronous wrapper that processes packages immediately and returns results.
     *
     * @param packages List of PackageRequestModel containing the package data to insert
     * @return BulkInsertResponseModel containing success/failure details for each package
     */
    com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreatePackages(java.util.List<PackageRequestModel> packages);
}