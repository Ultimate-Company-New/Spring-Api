package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Message entity operations.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByIsDeletedFalse();
    
    List<Message> findByIsDeletedFalseOrderByPublishDateDesc();
}

