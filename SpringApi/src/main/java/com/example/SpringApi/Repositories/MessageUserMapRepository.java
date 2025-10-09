package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MessageUserMap entity operations.
 */
@Repository
public interface MessageUserMapRepository extends JpaRepository<MessageUserMap, Long> {
    
    List<MessageUserMap> findByMessageId(Long messageId);
    
    List<MessageUserMap> findByUserId(Long userId);
    
    List<MessageUserMap> findByUserIdAndRecipientType(Long userId, String recipientType);
}

