package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.PackagePickupLocationMappingRequestModel;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.ResponseModels.PackagePickupLocationMappingResponseModel;
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
import java.util.Map;
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
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative);
        }
        
        if (end <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero);
        }
        
        if (start >= end) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd);
        }
        
        // Validate column name
        // Note: pickupLocationId filters through PackagePickupLocationMapping join
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
            "isDeleted",
            "pickupLocationId"
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
                    throw new BadRequestException(ErrorMessages.CommonErrorMessages.BooleanColumnsOnlySupportEquals);
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

        // Check if filtering by pickupLocationId to only include that location in response
        Long pickupLocationIdFilter = packageFilterQueryBuilder.extractPickupLocationIdFilter(paginationBaseRequestModel.getFilters());

        // Convert Package entities to PackageResponseModel with pickup location quantities
        PaginationBaseResponseModel<PackageResponseModel> response = new PaginationBaseResponseModel<>();
        List<PackageResponseModel> packageResponseModels = new ArrayList<>();
        for (Package pkg : page.getContent()) {
            PackageResponseModel packageResponseModel = new PackageResponseModel(pkg);
            
            // Fetch pickup location mappings and add to response with full inventory data
            List<PackagePickupLocationMapping> mappings = packagePickupLocationMappingRepository.findByPackageId(pkg.getPackageId());
            for (PackagePickupLocationMapping mapping : mappings) {
                // If filtering by pickupLocationId, only include that specific location
                if (pickupLocationIdFilter != null && !pickupLocationIdFilter.equals(mapping.getPickupLocationId())) {
                    continue;
                }
                PackagePickupLocationMappingResponseModel locationData = new PackagePickupLocationMappingResponseModel(mapping);
                packageResponseModel.getPickupLocationQuantities().put(mapping.getPickupLocationId(), locationData);
            }
            
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
     * @return PackageResponseModel with complete package details including pickup location quantities
     */
    @Override
    public PackageResponseModel getPackageById(Long packageId) {
        Package packageEntity = packageRepository.findByPackageIdAndClientId(packageId, getClientId());
        if (packageEntity == null) {
            throw new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId);
        }
        
        PackageResponseModel response = new PackageResponseModel(packageEntity);
        
        // Fetch pickup location mappings and add to response with full inventory data
        List<PackagePickupLocationMapping> mappings = packagePickupLocationMappingRepository.findByPackageId(packageId);
        for (PackagePickupLocationMapping mapping : mappings) {
            PackagePickupLocationMappingResponseModel locationData = new PackagePickupLocationMappingResponseModel(mapping);
            response.getPickupLocationQuantities().put(mapping.getPickupLocationId(), locationData);
        }
        
        return response;
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
    @Transactional
    public void updatePackage(PackageRequestModel packageRequest) {
        Package existingPackage = packageRepository.findByPackageIdAndClientId(packageRequest.getPackageId(), getClientId());
        if (existingPackage == null) {
            throw new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId);
        }

        // Create updated package using the constructor
        Package updatedPackage = new Package(packageRequest, getUser(), existingPackage);
        packageRepository.save(updatedPackage);

        // Fetch existing pickup location mappings before deleting (to compare quantities for lastRestockDate logic)
        List<PackagePickupLocationMapping> existingMappingsList = packagePickupLocationMappingRepository.findByPackageId(updatedPackage.getPackageId());
        Map<Long, PackagePickupLocationMapping> existingMappingsMap = existingMappingsList.stream()
            .collect(Collectors.toMap(PackagePickupLocationMapping::getPickupLocationId, m -> m));

        // Delete existing mappings
        packagePickupLocationMappingRepository.deleteByPackageId(updatedPackage.getPackageId());
        
        // Create new mappings with lastRestockDate logic (only update if quantity increased)
        if (packageRequest.getPickupLocationQuantities() != null && !packageRequest.getPickupLocationQuantities().isEmpty()) {
            createOrUpdatePickupLocationMappings(updatedPackage.getPackageId(), packageRequest.getPickupLocationQuantities(), getUser(), existingMappingsMap);
        }

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
        createPackage(packageRequest, getUser(), getClientId(), getUserId(), true);
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
                throw new BadRequestException(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Package"));
            }

            BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
            response.setTotalRequested(packages.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // Process each package individually
            for (PackageRequestModel packageRequest : packages) {
                try {
                    // Call createPackage with explicit createdUser, clientId, and shouldLog = false (bulk logs collectively)
                    createPackage(packageRequest, requestingUserLoginName, requestingClientId, requestingUserId, false);
                    
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
            throw new BadRequestException(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Package"));
        }

        BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
        response.setTotalRequested(packages.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        // Process each package individually
        for (PackageRequestModel packageRequest : packages) {
            try {
                // Call createPackage with current user, clientId, userId and shouldLog = false
                createPackage(packageRequest, getUser(), getClientId(), getUserId(), false);
                
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
     * Creates a new package in the system with explicit createdUser and clientId.
     * This variant is used for async operations where security context is not available.
     * 
     * @param packageRequest The package data to create
     * @param createdUser The loginName of the user creating this package (for async operations)
     * @param clientId The client ID for the package (for async operations)
     * @param userId The user ID for logging (for async operations)
     * @param shouldLog Whether to log this individual package creation (false for bulk operations)
     * @throws BadRequestException if the package data is invalid or incomplete
     */
    @Transactional
    protected void createPackage(PackageRequestModel packageRequest, String createdUser, Long clientId, Long userId, boolean shouldLog) {
        // Persist the package
        Package savedPackage = persistPackage(packageRequest, createdUser, clientId);

        // Create pickup location mappings if provided
        if (packageRequest.getPickupLocationQuantities() != null && !packageRequest.getPickupLocationQuantities().isEmpty()) {
            createPickupLocationMappings(savedPackage.getPackageId(), packageRequest.getPickupLocationQuantities(), createdUser);
        }

        // Log package creation (skip for bulk operations as they log collectively)
        if (shouldLog) {
            userLogService.logData(
                userId,
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

    /**
     * Creates pickup location mappings for a new package.
     * Sets lastRestockDate to UTC now for all new mappings.
     * Uses batch insert for optimized database performance.
     * 
     * @param packageId The package ID
     * @param pickupLocationQuantities Map of pickup location ID to PackagePickupLocationMappingRequestModel (quantity, reorderLevel, maxStockLevel, lastRestockDate, notes)
     * @param createdUser The loginName of the user creating these mappings
     */
    private void createPickupLocationMappings(Long packageId, Map<Long, PackagePickupLocationMappingRequestModel> pickupLocationQuantities, String createdUser) {
        // For new packages, always set lastRestockDate to UTC now
        createOrUpdatePickupLocationMappings(packageId, pickupLocationQuantities, createdUser, null);
    }

    /**
     * Creates or updates pickup location mappings for a package.
     * When updating, only sets lastRestockDate to UTC now if quantity increased.
     * Uses batch insert for optimized database performance.
     * 
     * @param packageId The package ID
     * @param pickupLocationQuantities Map of pickup location ID to PackagePickupLocationMappingRequestModel
     * @param modifiedUser The loginName of the user creating/updating these mappings
     * @param existingMappings Map of existing mappings (null for new packages, populated for updates)
     */
    private void createOrUpdatePickupLocationMappings(
            Long packageId, 
            Map<Long, PackagePickupLocationMappingRequestModel> pickupLocationQuantities, 
            String modifiedUser,
            Map<Long, PackagePickupLocationMapping> existingMappings) {
        
        List<PackagePickupLocationMapping> mappings = new ArrayList<>();
        java.time.LocalDateTime utcNow = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
        
        for (Map.Entry<Long, PackagePickupLocationMappingRequestModel> entry : pickupLocationQuantities.entrySet()) {
            Long pickupLocationId = entry.getKey();
            PackagePickupLocationMappingRequestModel locationData = entry.getValue();
            
            // Skip invalid entries
            if (pickupLocationId == null || pickupLocationId <= 0 || locationData == null) {
                continue;
            }
            
            int quantity = locationData.getQuantity() != null ? locationData.getQuantity() : 0;
            int reorderLevel = locationData.getReorderLevel() != null ? locationData.getReorderLevel() : 10;
            int maxStockLevel = locationData.getMaxStockLevel() != null ? locationData.getMaxStockLevel() : 1000;
            
            // Validate: quantity cannot be negative, maxStockLevel must be >= reorderLevel
            if (quantity < 0) {
                quantity = 0;
            }
            if (maxStockLevel < reorderLevel) {
                maxStockLevel = reorderLevel + 1;
            }
            
            // Determine lastRestockDate
            java.time.LocalDateTime lastRestockDate;
            if (existingMappings == null) {
                // New package: always set to UTC now
                lastRestockDate = utcNow;
            } else {
                // Update: check if quantity increased
                PackagePickupLocationMapping existingMapping = existingMappings.get(pickupLocationId);
                if (existingMapping == null) {
                    // New location for this package: set to UTC now
                    lastRestockDate = utcNow;
                } else {
                    // Existing location: only update if quantity increased
                    int oldQuantity = existingMapping.getAvailableQuantity() != null ? existingMapping.getAvailableQuantity() : 0;
                    if (quantity > oldQuantity) {
                        // Quantity increased: update lastRestockDate to UTC now
                        lastRestockDate = utcNow;
                    } else {
                        // Quantity same or decreased: keep existing lastRestockDate
                        lastRestockDate = existingMapping.getLastRestockDate();
                    }
                }
            }
            
            PackagePickupLocationMapping mapping = new PackagePickupLocationMapping();
            mapping.setPackageId(packageId);
            mapping.setPickupLocationId(pickupLocationId);
            mapping.setAvailableQuantity(quantity);
            mapping.setReorderLevel(reorderLevel);
            mapping.setMaxStockLevel(maxStockLevel);
            mapping.setLastRestockDate(lastRestockDate);
            mapping.setCreatedUser(modifiedUser);
            mapping.setModifiedUser(modifiedUser);
            
            mappings.add(mapping);
        }
        
        // Save all mappings in a single batch operation for better performance
        if (!mappings.isEmpty()) {
            packagePickupLocationMappingRepository.saveAll(mappings);
        }
    }
}
