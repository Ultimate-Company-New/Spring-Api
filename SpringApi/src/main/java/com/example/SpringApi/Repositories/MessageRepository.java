package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Message entity operations.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}

