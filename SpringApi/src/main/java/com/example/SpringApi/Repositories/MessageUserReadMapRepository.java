package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MessageUserReadMap entity operations.
 */
@Repository
public interface MessageUserReadMapRepository extends JpaRepository<MessageUserReadMap, Long> {
}

