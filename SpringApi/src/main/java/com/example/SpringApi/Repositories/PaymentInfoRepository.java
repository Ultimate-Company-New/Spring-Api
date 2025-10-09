package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PaymentInfo entity operations.
 */
@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
    
    List<PaymentInfo> findByUserId(Long userId);
    
    List<PaymentInfo> findByIsDeletedFalse();
    
    List<PaymentInfo> findByUserIdAndIsDeletedFalse(Long userId, Boolean isDeleted);
}

