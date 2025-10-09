package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PaymentInfo entity operations.
 */
@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
}

