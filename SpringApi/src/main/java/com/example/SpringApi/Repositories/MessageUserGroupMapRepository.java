package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserGroupMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MessageUserGroupMap entity operations.
 */
@Repository
public interface MessageUserGroupMapRepository extends JpaRepository<MessageUserGroupMap, Long> {
    
    List<MessageUserGroupMap> findByMessageId(Long messageId);
    
    List<MessageUserGroupMap> findByGroupId(Long groupId);
}

