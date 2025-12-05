package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
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
import org.springframework.data.domain.PageRequest;
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
    private final com.example.SpringApi.Repositories.PickupLocationRepository pickupLocationRepository;
    private final PackageFilterQueryBuilder packageFilterQueryBuilder;
    private final MessageService messageService;

    @Autowired
    public PackageService(
        PackageRepository packageRepository,
        UserLogService userLogService,
        PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
        com.example.SpringApi.Repositories.PickupLocationRepository pickupLocationRepository,
        PackageFilterQueryBuilder packageFilterQueryBuilder,
        MessageService messageService,
        HttpServletRequest request) {
        super();
        this.packageRepository = packageRepository;
        this.userLogService = userLogService;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.pickupLocationRepository = pickupLocationRepository;
        this.packageFilterQueryBuilder = packageFilterQueryBuilder;
        this.messageService = messageService;
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
        // Validate pagination parameters
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        
        if (start < 0) {
            throw new BadRequestException("Start index cannot be negative");
        }
        
        if (end <= 0) {
            throw new BadRequestException("End index must be greater than 0");
        }
        
        if (start >= end) {
            throw new BadRequestException("Start index must be less than end index");
        }
        
        // Validate column name
        Set<String> validColumns = new HashSet<>(Arrays.asList(
            "packageId",
            "packageName",
            "dimensions",
            "length",
            "breadth",
            "height",
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
        
        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException("Invalid column name: " + filter.getColumn());
                }

                // Validate operator
                Set<String> validOperators = new HashSet<>(Arrays.asList(
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
                ));
                if (filter.getOperator() != null && !validOperators.contains(filter.getOperator())) {
                    throw new BadRequestException("Invalid operator: " + filter.getOperator());
                }

                // Validate column type matches operator
                String columnType = packageFilterQueryBuilder.getColumnType(filter.getColumn());
                if ("boolean".equals(columnType) && !filter.getOperator().equals("equals") && !filter.getOperator().equals("notEquals")) {
                    throw new BadRequestException("Boolean columns only support 'equals' and 'notEquals' operators");
                }
                if ("date".equals(columnType) || "number".equals(columnType)) {
                    Set<String> numericDateOperators = new HashSet<>(Arrays.asList(
                        "equals", "notEquals", "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
                    ));
                    if (!numericDateOperators.contains(filter.getOperator())) {
                        throw new BadRequestException(columnType + " columns only support numeric comparison operators");
                    }
                }
            }
        }
        
        int limit = end - start;
        
        // Create custom Pageable with exact OFFSET and LIMIT for database-level pagination
        org.springframework.data.domain.Pageable pageable = new PageRequest(0, limit, Sort.by("packageId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };
        
        // Use filter query builder for dynamic filtering
        Page<Package> page = packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
            paginationBaseRequestModel.getFilters(),
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
     * Retrieves all packages in the system for the current client.
     *
     * @return List of all PackageResponseModel entities in the system
     */
    @Override
    public List<PackageResponseModel> getAllPackagesInSystem() {
        return packageRepository.findAll().stream()
            .filter(pkg -> pkg.getClientId().equals(getClientId()))
            .map(PackageResponseModel::new)
            .collect(Collectors.toList());
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
        persistPackage(packageRequest);
    }

    private Package persistPackage(PackageRequestModel packageRequest) {
        Package savedPackage = packageRepository.save(new Package(packageRequest, getUser(), getClientId()));

        // Make generated ID available to the caller (useful for bulk operations/tests)
        packageRequest.setPackageId(savedPackage.getPackageId());

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.PackagesSuccessMessages.InsertPackage + savedPackage.getPackageId(),
            ApiRoutes.PackageSubRoute.CREATE_PACKAGE);
        return savedPackage;
    }

    /**
     * Retrieves all packages available at a specific pickup location.
     *
     * @param pickupLocationId The unique identifier of the pickup location
     * @return List of PackageResponseModel entities available at the pickup location
     * @throws NotFoundException if the pickup location is not found
     */
    @Override
    public List<PackageResponseModel> getPackagesByPickupLocationId(Long pickupLocationId) {
        // Validate that the pickup location exists
        if (pickupLocationRepository.countByPickupLocationIdAndClientId(pickupLocationId, getClientId()) == 0) {
            throw new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId));
        }
        
        List<PackagePickupLocationMapping> mappings = packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(pickupLocationId, getClientId());
        return mappings.stream()
            .map(mapping -> new PackageResponseModel(mapping.getPackageEntity()))
            .collect(Collectors.toList());
    }

    /**
     * Creates multiple packages in a single operation.
     *
     * @param packages List of PackageRequestModel containing the package data to insert
     * @return BulkInsertResponseModel containing success/failure details for each package
     */
    @Override
    public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreatePackages(java.util.List<PackageRequestModel> packages) {
        if (packages == null || packages.isEmpty()) {
            throw new BadRequestException("Package list cannot be null or empty");
        }

        com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = 
            new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
        response.setTotalRequested(packages.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (PackageRequestModel packageRequest : packages) {
            try {
                Package createdPackage = persistPackage(packageRequest);
                response.addSuccess(packageRequest.getPackageName(), createdPackage.getPackageId());
                successCount++;
            } catch (BadRequestException bre) {
                response.addFailure(
                    packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                response.addFailure(
                    packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        userLogService.logData(getUserId(), 
            SuccessMessages.PackagesSuccessMessages.InsertPackage + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.PackageSubRoute.BULK_CREATE_PACKAGE);
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        BulkInsertHelper.createBulkInsertResultMessage(response, "Package", messageService, getUserId(), getUser(), getClientId());
        
        return response;
    }
}