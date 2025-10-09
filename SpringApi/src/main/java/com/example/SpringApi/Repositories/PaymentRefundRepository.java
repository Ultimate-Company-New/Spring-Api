package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PaymentRefund entity operations.
 */
@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
}

