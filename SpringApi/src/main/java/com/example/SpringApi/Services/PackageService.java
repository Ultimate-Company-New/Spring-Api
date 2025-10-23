package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Repositories.PackageRepository;
import com.example.SpringApi.Services.Interface.IPackageSubTranslator;

import jakarta.servlet.http.HttpServletRequest;

import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Service implementation for Package operations and business logic.
 * Handles all package-related business operations including CRUD operations,
 * batch processing, validation, and specialized queries.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PackageService extends BaseService implements IPackageSubTranslator {
    private final PackageRepository packageRepository;
    private final UserLogService userLogService;
    private final PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Autowired
    public PackageService(
        PackageRepository packageRepository,
        UserLogService userLogService,
        PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
        HttpServletRequest request) {
        super();
        this.packageRepository = packageRepository;
        this.userLogService = userLogService;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
    }

    /**
     * Retrieves packages in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     *
     * @param paginationBaseRequestModel The request model containing pagination and filter parameters
     * @return PaginationBaseResponseModel containing paginated package data
     */
    @Override
    public PaginationBaseResponseModel<PackageResponseModel> getPackagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Validate column name
        Set<String> validColumns = new HashSet<>(Arrays.asList(
            "packageId",
            "packageName",
            "dimensions",
            "standardCapacity",
            "packageType",
            "maxWeight",
            "pricePerUnit",
            "createdUser",
            "modifiedUser",
            "createdAt",
            "updatedAt",
            "notes",
            "isDeleted"
        ));
        
        if (paginationBaseRequestModel.getColumnName() != null && 
            !validColumns.contains(paginationBaseRequestModel.getColumnName())) {
            throw new BadRequestException("Invalid column name for filtering: " + paginationBaseRequestModel.getColumnName());
        }
        
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int limit = end - start;
        
        // Create custom Pageable with exact OFFSET and LIMIT for database-level pagination
        // Spring's PageRequest.of(page, size) uses: OFFSET = page * size, LIMIT = size
        // For arbitrary offsets (e.g., start=5, end=15), we need OFFSET=5, LIMIT=10
        // Solution: Override getOffset() to return the exact start position
        org.springframework.data.domain.Pageable pageable = new org.springframework.data.domain.PageRequest(0, limit, Sort.by("packageId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };
        
        // Execute paginated query with filtering - Page object handles count automatically
        Page<Package> page = packageRepository.findPaginatedPackages(
            getClientId(),
            paginationBaseRequestModel.getColumnName(),
            paginationBaseRequestModel.getCondition(),
            paginationBaseRequestModel.getFilterExpr(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Convert Package entities to PackageResponseModel
        PaginationBaseResponseModel<PackageResponseModel> response = new PaginationBaseResponseModel<>();
        List<PackageResponseModel> packageResponseModels = new ArrayList<>();
        for (Package pkg : page.getContent()) {
            PackageResponseModel packageResponseModel = new PackageResponseModel(pkg);
            packageResponseModels.add(packageResponseModel);
        }
        
        // Set the total count of all filtered records
        response.setData(packageResponseModels);
        response.setTotalDataCount(page.getTotalElements());

        return response;
    }    
    
    /**
     * Retrieves detailed information for a specific package by ID.
     *
     * @param packageId The unique identifier of the package
     * @return PackageResponseModel with complete package details
     */
    @Override
    public PackageResponseModel getPackageById(Long packageId) {
        Package packageEntity = packageRepository.findByPackageIdAndClientId(packageId, getClientId());
        if (packageEntity == null) {
            throw new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId);
        }
        return new PackageResponseModel(packageEntity);
    }

    /**
     * Retrieves all packages in the system.
     *
     * @return List of all PackageResponseModel entities in the system
     */
    @Override
    public List<PackageResponseModel> getAllPackagesInSystem() {
        List<Package> packages = packageRepository.findByClientIdAndIsDeletedFalse(getClientId());
        return packages.stream()
            .map(PackageResponseModel::new)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Toggles the active status of a package.
     *
     * @param packageId The unique identifier of the package to toggle
     */
    @Override
    public void togglePackage(Long packageId) {
        Package packageEntity = packageRepository.findByPackageIdAndClientId(packageId, getClientId());
        if (packageEntity == null) {
            throw new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId);
        }

        // Toggle the deleted status (active/inactive)
        packageEntity.setIsDeleted(!packageEntity.getIsDeleted());
        packageEntity.setModifiedUser(getUser());

        packageRepository.save(packageEntity);

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PackagesSuccessMessages.TogglePackage + packageEntity.getPackageId(),
            ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE);
    }

    /**
     * Updates an existing package with new information.
     *
     * @param packageRequest The updated package data
     */
    @Override
    public void updatePackage(PackageRequestModel packageRequest) {
        Package existingPackage = packageRepository.findByPackageIdAndClientId(packageRequest.getPackageId(), getClientId());
        if (existingPackage == null) {
            throw new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId);
        }

        // Create updated package using the constructor
        Package updatedPackage = new Package(packageRequest, getUser(), existingPackage);
        packageRepository.save(updatedPackage);

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PackagesSuccessMessages.UpdatePackage + updatedPackage.getPackageId(),
            ApiRoutes.PackageSubRoute.UPDATE_PACKAGE);
    }

    /**
     * Creates a new package in the system.
     *
     * @param packageRequest The package data to create
     */
    @Override
    public void createPackage(PackageRequestModel packageRequest) {
        // Create new package using the constructor
        Package savedPackage =packageRepository.save(new Package(packageRequest, getUser(), getClientId()));

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PackagesSuccessMessages.InsertPackage + savedPackage.getPackageId(),
            ApiRoutes.PackageSubRoute.CREATE_PACKAGE);
    }

    /**
     * Retrieves all packages available at a specific pickup location.
     *
     * @param pickupLocationId The unique identifier of the pickup location
     * @return List of PackageResponseModel entities available at the pickup location
     */
    @Override
    public List<PackageResponseModel> getPackagesByPickupLocationId(Long pickupLocationId) {
        List<PackagePickupLocationMapping> mappings = packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(pickupLocationId, getClientId());
        return mappings.stream()
            .map(mapping -> new PackageResponseModel(mapping.getPackageEntity()))
            .collect(Collectors.toList());
    }
}