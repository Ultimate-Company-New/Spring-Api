package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository interface for MessageUserMap entity operations. */
@Repository
public interface MessageUserMapRepository extends JpaRepository<MessageUserMap, Long> {

  /**
   * Deletes all MessageUserMap entries for a specific message.
   *
   * @param messageId The message ID
   */
  @Modifying
  @Transactional
  void deleteByMessageId(Long messageId);
}

