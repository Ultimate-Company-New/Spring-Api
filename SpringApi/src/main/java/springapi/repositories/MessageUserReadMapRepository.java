package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.MessageUserReadMap;

/** Repository interface for MessageUserReadMap entity operations. */
@Repository
public interface MessageUserReadMapRepository extends JpaRepository<MessageUserReadMap, Long> {

  @Query(
      "SELECT murm FROM MessageUserReadMap murm WHERE murm.messageId = :"
          + "messageId AND murm.userId = :userId")
  MessageUserReadMap findByMessageIdAndUserId(
      @Param("messageId") Long messageId, @Param("userId") Long userId);
}
