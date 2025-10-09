package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentRefundProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PaymentRefundProductMapping entity operations.
 */
@Repository
public interface PaymentRefundProductMappingRepository extends JpaRepository<PaymentRefundProductMapping, Long> {
    
    List<PaymentRefundProductMapping> findByRefundId(Long refundId);
    
    List<PaymentRefundProductMapping> findByProductId(Long productId);
}

