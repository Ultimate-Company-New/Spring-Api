package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MessageUserMap entity operations.
 */
@Repository
public interface MessageUserMapRepository extends JpaRepository<MessageUserMap, Long> {
}

