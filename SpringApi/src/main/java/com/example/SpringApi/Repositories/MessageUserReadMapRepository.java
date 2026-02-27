package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for MessageUserReadMap entity operations. */
@Repository
public interface MessageUserReadMapRepository extends JpaRepository<MessageUserReadMap, Long> {

  @Query(
      "SELECT murm FROM MessageUserReadMap murm WHERE murm.messageId = :messageId AND murm.userId = :userId")
  MessageUserReadMap findByMessageIdAndUserId(
      @Param("messageId") Long messageId, @Param("userId") Long userId);
}
