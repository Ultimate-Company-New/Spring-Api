package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.ShipmentFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;
import com.example.SpringApi.Repositories.ShipmentRepository;
import com.example.SpringApi.Services.Interface.IShipmentSubTranslator;
import org.hibernate.Hibernate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Shipment operations.
 * 
 * This service handles all business logic related to shipment management,
 * including retrieval, filtering, and pagination.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ShipmentService extends BaseService implements IShipmentSubTranslator {
    
    private final ShipmentRepository shipmentRepository;
    private final ShipmentFilterQueryBuilder shipmentFilterQueryBuilder;
    
    // Valid columns for filtering
    private static final Set<String> VALID_COLUMNS = Set.of(
        "shipmentId", "orderSummaryId", "pickupLocationId", "totalWeightKgs", "totalQuantity",
        "expectedDeliveryDate", "packagingCost", "shippingCost", "totalCost",
        "selectedCourierCompanyId", "selectedCourierName", "selectedCourierRate", "selectedCourierMinWeight",
        "shipRocketOrderId", "shipRocketShipmentId", "shipRocketAwbCode", "shipRocketTrackingId", "shipRocketStatus",
        "createdUser", "modifiedUser", "createdAt", "updatedAt"
    );
    
    public ShipmentService(ShipmentRepository shipmentRepository, 
                          ShipmentFilterQueryBuilder shipmentFilterQueryBuilder) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentFilterQueryBuilder = shipmentFilterQueryBuilder;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationBaseResponseModel<ShipmentResponseModel> getShipmentsInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !VALID_COLUMNS.contains(filter.getColumn())) {
                    throw new BadRequestException(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, filter.getColumn()));
                }

                // Validate operator (FilterCondition.setOperator auto-normalizes symbols to words)
                if (!filter.isValidOperator()) {
                    throw new BadRequestException(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, filter.getOperator()));
                }

                // Validate column type matches operator
                String columnType = shipmentFilterQueryBuilder.getColumnType(filter.getColumn());
                filter.validateOperatorForType(columnType, filter.getColumn());

                // Validate value presence
                filter.validateValuePresence();
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("createdAt").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Use filter query builder for dynamic filtering
        Page<Shipment> result = shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
            paginationBaseRequestModel.getFilters(),
            pageable
        );

        // Convert Shipment results to ShipmentResponseModel
        // Note: Lazy-loaded entities must be initialized before mapping to response model
        PaginationBaseResponseModel<ShipmentResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(result.getContent().stream()
            .map(shipment -> {
                // Initialize lazy-loaded entities
                Hibernate.initialize(shipment.getOrderSummary());
                if (shipment.getOrderSummary() != null) {
                    Hibernate.initialize(shipment.getOrderSummary().getEntityAddress());
                }
                Hibernate.initialize(shipment.getPickupLocation());
                if (shipment.getPickupLocation() != null) {
                    Hibernate.initialize(shipment.getPickupLocation().getAddress());
                }
                
                // Initialize shipment products and their nested Product entities
                Hibernate.initialize(shipment.getShipmentProducts());
                if (shipment.getShipmentProducts() != null) {
                    for (var sp : shipment.getShipmentProducts()) {
                        Hibernate.initialize(sp.getProduct());
                    }
                }
                
                // Initialize shipment packages and their nested Package and Product entities
                Hibernate.initialize(shipment.getShipmentPackages());
                if (shipment.getShipmentPackages() != null) {
                    for (var pkg : shipment.getShipmentPackages()) {
                        Hibernate.initialize(pkg.getPackageInfo());
                        Hibernate.initialize(pkg.getShipmentPackageProducts());
                        if (pkg.getShipmentPackageProducts() != null) {
                            for (var spp : pkg.getShipmentPackageProducts()) {
                                Hibernate.initialize(spp.getProduct());
                            }
                        }
                    }
                }
                
                // Initialize return shipments and their products
                Hibernate.initialize(shipment.getReturnShipments());
                if (shipment.getReturnShipments() != null) {
                    for (var rs : shipment.getReturnShipments()) {
                        Hibernate.initialize(rs.getReturnProducts());
                    }
                }
                
                return new ShipmentResponseModel(shipment);
            })
            .collect(Collectors.toList()));
        response.setTotalDataCount(result.getTotalElements());

        return response;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ShipmentResponseModel getShipmentById(Long shipmentId) {
        if (shipmentId == null || shipmentId <= 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.InvalidId);
        }
        
        Long clientId = getClientId();
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId)));
        
        // Verify client access
        if (!shipment.getClientId().equals(clientId)) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId));
        }
        
        // Only return shipments with ShipRocket order ID assigned
        if (shipment.getShipRocketOrderId() == null || shipment.getShipRocketOrderId().trim().isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId));
        }
        
        // Initialize lazy-loaded entities
        Hibernate.initialize(shipment.getOrderSummary());
        if (shipment.getOrderSummary() != null) {
            Hibernate.initialize(shipment.getOrderSummary().getEntityAddress());
        }
        Hibernate.initialize(shipment.getPickupLocation());
        if (shipment.getPickupLocation() != null) {
            Hibernate.initialize(shipment.getPickupLocation().getAddress());
        }
        
        // Initialize shipment products and their nested Product entities
        Hibernate.initialize(shipment.getShipmentProducts());
        if (shipment.getShipmentProducts() != null) {
            for (var sp : shipment.getShipmentProducts()) {
                Hibernate.initialize(sp.getProduct());
            }
        }
        
        // Initialize shipment packages and their nested Package and Product entities
        Hibernate.initialize(shipment.getShipmentPackages());
        if (shipment.getShipmentPackages() != null) {
            for (var pkg : shipment.getShipmentPackages()) {
                Hibernate.initialize(pkg.getPackageInfo());
                Hibernate.initialize(pkg.getShipmentPackageProducts());
                if (pkg.getShipmentPackageProducts() != null) {
                    for (var spp : pkg.getShipmentPackageProducts()) {
                        Hibernate.initialize(spp.getProduct());
                    }
                }
            }
        }
        
        // Initialize return shipments and their products
        Hibernate.initialize(shipment.getReturnShipments());
        if (shipment.getReturnShipments() != null) {
            for (var rs : shipment.getReturnShipments()) {
                Hibernate.initialize(rs.getReturnProducts());
            }
        }
        
        return new ShipmentResponseModel(shipment);
    }
}
