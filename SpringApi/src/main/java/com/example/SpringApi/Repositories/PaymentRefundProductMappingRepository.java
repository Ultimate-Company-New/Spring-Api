package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentRefundProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PaymentRefundProductMapping entity operations.
 */
@Repository
public interface PaymentRefundProductMappingRepository extends JpaRepository<PaymentRefundProductMapping, Long> {
}

