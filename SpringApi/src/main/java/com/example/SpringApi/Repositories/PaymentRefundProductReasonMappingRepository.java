package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentRefundProductReasonMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PaymentRefundProductReasonMapping entity operations.
 */
@Repository
public interface PaymentRefundProductReasonMappingRepository extends JpaRepository<PaymentRefundProductReasonMapping, Long> {
    
    List<PaymentRefundProductReasonMapping> findByRefundId(Long refundId);
    
    List<PaymentRefundProductReasonMapping> findByReasonCode(String reasonCode);
}

