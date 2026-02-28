package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import springapi.models.databasemodels.MessageUserGroupMap;

/** Repository interface for MessageUserGroupMap entity operations. */
@Repository
public interface MessageUserGroupMapRepository extends JpaRepository<MessageUserGroupMap, Long> {

  /**
   * Deletes all MessageUserGroupMap entries for a specific message.
   *
   * @param messageId The message ID
   */
  @Modifying
  @Transactional
  void deleteByMessageId(Long messageId);
}
