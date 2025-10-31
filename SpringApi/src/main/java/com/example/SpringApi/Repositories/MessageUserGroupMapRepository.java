package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserGroupMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MessageUserGroupMap entity operations.
 */
@Repository
public interface MessageUserGroupMapRepository extends JpaRepository<MessageUserGroupMap, Long> {
    
    /**
     * Deletes all MessageUserGroupMap entries for a specific message.
     * 
     * @param messageId The message ID
     */
    void deleteByMessageId(Long messageId);
}

