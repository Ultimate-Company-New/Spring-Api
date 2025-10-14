package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
            "dimensions",
            "quantity"
        ));
        
        if (paginationBaseRequestModel.getColumnName() != null && 
            !validColumns.contains(paginationBaseRequestModel.getColumnName())) {
            throw new BadRequestException("Invalid column name for filtering: " + paginationBaseRequestModel.getColumnName());
        }
        
        // Calculate page number from start and pageSize
        int pageNumber = paginationBaseRequestModel.getStart() / paginationBaseRequestModel.getPageSize();
        
        // Create pageable with basic sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "packageId");
        Pageable pageable = PageRequest.of(pageNumber, paginationBaseRequestModel.getPageSize(), sort);

                        // Execute paginated query with filtering
        Page<Package> result = packageRepository.findPaginatedPackages(
            paginationBaseRequestModel.getColumnName(),
            paginationBaseRequestModel.getCondition(),
            paginationBaseRequestModel.getFilterExpr(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Convert Package entities to PackageResponseModel
        PaginationBaseResponseModel<PackageResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(result.getContent().stream()
            .map(PackageResponseModel::new)
            .collect(Collectors.toList()));
        response.setTotalDataCount(result.getTotalElements());

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
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId));
        return new PackageResponseModel(packageEntity);
    }

    /**
     * Retrieves all packages in the system.
     *
     * @return List of all PackageResponseModel entities in the system
     */
    @Override
    public List<PackageResponseModel> getAllPackagesInSystem() {
        List<Package> packages = packageRepository.findAll();
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
        Package packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId));

        // Toggle the deleted status (active/inactive)
        packageEntity.setIsDeleted(!packageEntity.getIsDeleted());
        packageEntity.setModifiedUser(getUser());

        packageRepository.save(packageEntity);

        // Logging
        userLogService.logData(
            getUser(),
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
        // Validate address for pickup location
        validateAddress(packageRequest.getAddress());
        
        Package existingPackage = packageRepository.findById(packageRequest.getPackageId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.PackageErrorMessages.InvalidId));

        // Update the existing package with new data
        existingPackage.setPackageName(packageRequest.getPackageName());
        existingPackage.setLength(packageRequest.getLength());
        existingPackage.setBreadth(packageRequest.getBreadth());
        existingPackage.setHeight(packageRequest.getHeight());
        existingPackage.setMaxWeight(packageRequest.getMaxWeight());
        existingPackage.setStandardCapacity(packageRequest.getStandardCapacity());
        existingPackage.setPricePerUnit(packageRequest.getPricePerUnit());
        existingPackage.setPackageType(packageRequest.getPackageType());
        existingPackage.setClientId(packageRequest.getClientId());
        existingPackage.setIsDeleted(packageRequest.getIsDeleted());
        existingPackage.setModifiedUser(getUser());
        
        Package savedPackage = packageRepository.save(existingPackage);

        // Logging
        userLogService.logData(
            getUser(),
            SuccessMessages.PackagesSuccessMessages.UpdatePackage + savedPackage.getPackageId(),
            ApiRoutes.PackageSubRoute.UPDATE_PACKAGE);
    }

    /**
     * Creates a new package in the system.
     *
     * @param packageRequest The package data to create
     */
    @Override
    public void createPackage(PackageRequestModel packageRequest) {
        // Validate address for pickup location
        validateAddress(packageRequest.getAddress());
        
        // Create a new package entity
        Package newPackage = new Package();
        newPackage.setPackageName(packageRequest.getPackageName());
        newPackage.setLength(packageRequest.getLength());
        newPackage.setBreadth(packageRequest.getBreadth());
        newPackage.setHeight(packageRequest.getHeight());
        newPackage.setMaxWeight(packageRequest.getMaxWeight());
        newPackage.setStandardCapacity(packageRequest.getStandardCapacity());
        newPackage.setPricePerUnit(packageRequest.getPricePerUnit());
        newPackage.setPackageType(packageRequest.getPackageType());
        newPackage.setClientId(packageRequest.getClientId());
        newPackage.setIsDeleted(packageRequest.getIsDeleted() != null ? packageRequest.getIsDeleted() : Boolean.FALSE);
        newPackage.setCreatedUser(getUser());
        newPackage.setModifiedUser(getUser());
        
        Package savedPackage = packageRepository.save(newPackage);

        // Logging
        userLogService.logData(
            getUser(),
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
        List<PackagePickupLocationMapping> mappings = packagePickupLocationMappingRepository.findByPickupLocationId(pickupLocationId);
        return mappings.stream()
            .map(mapping -> new PackageResponseModel(mapping.getPackageEntity()))
            .collect(Collectors.toList());
    }

    /**
     * Validates the address for pickup location requirements.
     *
     * @param address The address to validate
     */
    private void validateAddress(AddressRequestModel address) {
        if (address == null) {
            throw new BadRequestException("Address is required for package validation");
        }
        
        // Validate required address fields
        if (address.getStreetAddress() == null || address.getStreetAddress().trim().isEmpty()) {
            throw new BadRequestException("Street address is required");
        }
        
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new BadRequestException("City is required");
        }
        
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new BadRequestException("State is required");
        }
        
        if (address.getPostalCode() == null || address.getPostalCode().trim().isEmpty()) {
            throw new BadRequestException("Postal code is required");
        }
        
        // Additional validation can be added here as needed
    }
}