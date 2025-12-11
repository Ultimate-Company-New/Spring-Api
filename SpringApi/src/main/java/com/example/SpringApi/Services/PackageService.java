package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void createPackage(PackageRequestModel packageRequest) {
        createPackage(packageRequest, getUser(), true);
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
     * Creates multiple packages asynchronously in the system with partial success support.
     * 
     * This method processes packages in a background thread with the following characteristics:
     * - Supports partial success: if some packages fail validation, others still succeed
     * - Sends detailed results to user via message notification after processing completes
     * - NOT_SUPPORTED: Runs without a transaction to avoid rollback-only issues when individual package creations fail
     * 
     * @param packages List of PackageRequestModel containing the package data to create
     * @param requestingUserId The ID of the user making the request (captured from security context)
     * @param requestingUserLoginName The loginName of the user making the request (captured from security context)
     * @param requestingClientId The client ID of the user making the request (captured from security context)
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void bulkCreatePackagesAsync(List<PackageRequestModel> packages, Long requestingUserId, String requestingUserLoginName, Long requestingClientId) {
        try {
            // Validate input
            if (packages == null || packages.isEmpty()) {
                throw new BadRequestException("Package list cannot be null or empty");
            }

            BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
            response.setTotalRequested(packages.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // Process each package individually
            for (PackageRequestModel packageRequest : packages) {
                try {
                    // Call createPackage with explicit createdUser and shouldLog = false (bulk logs collectively)
                    createPackage(packageRequest, requestingUserLoginName, false);
                    
                    // If we get here, package was created successfully
                    // The packageId is set by persistPackage method
                    response.addSuccess(packageRequest.getPackageName(), packageRequest.getPackageId());
                    successCount++;
                    
                } catch (BadRequestException bre) {
                    // Validation or business logic error
                    response.addFailure(
                        packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                        bre.getMessage()
                    );
                    failureCount++;
                } catch (Exception e) {
                    // Unexpected error
                    response.addFailure(
                        packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                        "Error: " + e.getMessage()
                    );
                    failureCount++;
                }
            }
            
            // Log bulk package creation (using captured context values)
            userLogService.logDataWithContext(
                requestingUserId,
                requestingUserLoginName,
                requestingClientId,
                SuccessMessages.PackagesSuccessMessages.InsertPackage + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
                ApiRoutes.PackageSubRoute.BULK_CREATE_PACKAGE
            );
            
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);
            
            // Create a message with the bulk insert results using the helper (using captured context)
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                response, "Package", "Packages", "Package Name", "Package ID", 
                messageService, requestingUserId, requestingUserLoginName, requestingClientId
            );
            
        } catch (Exception e) {
            // Still send a message to user about the failure (using captured userId)
            BulkInsertResponseModel<Long> errorResponse = new BulkInsertResponseModel<>();
            errorResponse.setTotalRequested(packages != null ? packages.size() : 0);
            errorResponse.setSuccessCount(0);
            errorResponse.setFailureCount(packages != null ? packages.size() : 0);
            errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                errorResponse, "Package", "Packages", "Package Name", "Package ID", 
                messageService, requestingUserId, requestingUserLoginName, requestingClientId
            );
        }
    }

    /**
     * Creates multiple packages synchronously in a single operation (for testing).
     * This is a synchronous wrapper that processes packages immediately and returns results.
     * 
     * @param packages List of PackageRequestModel containing the package data to create
     * @return BulkInsertResponseModel containing success/failure details for each package
     */
    @Override
    @Transactional
    public BulkInsertResponseModel<Long> bulkCreatePackages(List<PackageRequestModel> packages) {
        // Validate input
        if (packages == null || packages.isEmpty()) {
            throw new BadRequestException("Package list cannot be null or empty");
        }

        BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
        response.setTotalRequested(packages.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        // Process each package individually
        for (PackageRequestModel packageRequest : packages) {
            try {
                // Call createPackage with current user and shouldLog = false
                createPackage(packageRequest, getUser(), false);
                
                // If we get here, package was created successfully
                response.addSuccess(packageRequest.getPackageName(), packageRequest.getPackageId());
                successCount++;
                
            } catch (BadRequestException bre) {
                // Validation or business logic error
                response.addFailure(
                    packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                // Unexpected error
                response.addFailure(
                    packageRequest.getPackageName() != null ? packageRequest.getPackageName() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        // Log bulk package creation
        userLogService.logData(
            getUserId(),
            SuccessMessages.PackagesSuccessMessages.InsertPackage + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.PackageSubRoute.BULK_CREATE_PACKAGE
        );
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        return response;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a new package in the system with explicit createdUser.
     * This variant is used for async operations where security context is not available.
     * 
     * @param packageRequest The package data to create
     * @param createdUser The loginName of the user creating this package (for async operations)
     * @param shouldLog Whether to log this individual package creation (false for bulk operations)
     * @throws BadRequestException if the package data is invalid or incomplete
     */
    @Transactional
    private void createPackage(PackageRequestModel packageRequest, String createdUser, boolean shouldLog) {
        // Get security context
        Long currentClientId = getClientId();
        Long currentUserId = getUserId();

        // Persist the package
        Package savedPackage = persistPackage(packageRequest, createdUser, currentClientId);

        // Log package creation (skip for bulk operations as they log collectively)
        if (shouldLog) {
            userLogService.logData(
                currentUserId,
                SuccessMessages.PackagesSuccessMessages.InsertPackage + savedPackage.getPackageId(),
                ApiRoutes.PackageSubRoute.CREATE_PACKAGE
            );
        }
    }

    /**
     * Persists a package to the database.
     * 
     * @param packageRequest The package data to persist
     * @param createdUser The loginName of the user creating this package
     * @param clientId The client ID for the package
     * @return The saved Package entity
     */
    private Package persistPackage(PackageRequestModel packageRequest, String createdUser, Long clientId) {
        Package savedPackage = packageRepository.save(new Package(packageRequest, createdUser, clientId));

        // Make generated ID available to the caller (useful for bulk operations/tests)
        packageRequest.setPackageId(savedPackage.getPackageId());

        return savedPackage;
    }
}
