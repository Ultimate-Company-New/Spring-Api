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
     * Retrieves all packages in the system.
     *
     * @return List of all PackageResponseModel entities in the system
     */
    List<PackageResponseModel> getAllPackagesInSystem();

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
     * Creates multiple packages in a single operation.
     *
     * @param packages List of PackageRequestModel containing the package data to insert
     * @return BulkInsertResponseModel containing success/failure details for each package
     */
    com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreatePackages(java.util.List<PackageRequestModel> packages);
}