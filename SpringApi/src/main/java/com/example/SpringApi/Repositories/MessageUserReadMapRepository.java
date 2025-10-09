package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MessageUserReadMap entity operations.
 */
@Repository
public interface MessageUserReadMapRepository extends JpaRepository<MessageUserReadMap, Long> {
    
    List<MessageUserReadMap> findByMessageId(Long messageId);
    
    List<MessageUserReadMap> findByUserId(Long userId);
    
    Optional<MessageUserReadMap> findByMessageIdAndUserId(Long messageId, Long userId);
}

