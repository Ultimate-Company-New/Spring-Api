package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Message;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for Message entity operations. */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query(
      "SELECT DISTINCT m FROM Message m "
          + "LEFT JOIN m.messageUserMaps mum "
          + "LEFT JOIN m.messageUserGroupMaps mugm "
          + "WHERE m.clientId = :clientId "
          + "AND (:includeDeleted = true OR m.isDeleted = false) "
          + "AND (COALESCE(:filterExpr, '') = '' OR "
          + "(CASE :columnName "
          + "WHEN 'messageId' THEN CONCAT(m.messageId, '') "
          + "WHEN 'title' THEN CONCAT(m.title, '') "
          + "WHEN 'publishDate' THEN CONCAT(m.publishDate, '') "
          + "WHEN 'descriptionHtml' THEN CONCAT(m.descriptionHtml, '') "
          + "WHEN 'sendAsEmail' THEN CASE WHEN m.sendAsEmail = true THEN 'true' ELSE 'false' END "
          + "WHEN 'isDeleted' THEN CASE WHEN m.isDeleted = true THEN 'true' ELSE 'false' END "
          + "WHEN 'createdByUserId' THEN CONCAT(m.createdByUserId, '') "
          + "WHEN 'sendgridEmailBatchId' THEN CONCAT(m.sendgridEmailBatchId, '') "
          + "WHEN 'createdAt' THEN CONCAT(m.createdAt, '') "
          + "WHEN 'updatedAt' THEN CONCAT(m.updatedAt, '') "
          + "WHEN 'notes' THEN CONCAT(m.notes, '') "
          + "WHEN 'createdUser' THEN CONCAT(m.createdUser, '') "
          + "WHEN 'modifiedUser' THEN CONCAT(m.modifiedUser, '') "
          + "ELSE '' END) LIKE "
          + "(CASE :condition "
          + "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') "
          + "WHEN 'equals' THEN :filterExpr "
          + "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') "
          + "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) "
          + "WHEN 'isEmpty' THEN '' "
          + "WHEN 'isNotEmpty' THEN '%' "
          + "ELSE '' END))")
  Page<Message> findPaginatedMessages(
      @Param("clientId") Long clientId,
      @Param("columnName") String columnName,
      @Param("condition") String condition,
      @Param("filterExpr") String filterExpr,
      @Param("includeDeleted") boolean includeDeleted,
      Pageable pageable);

  @Query(
      "SELECT m FROM Message m "
          + "LEFT JOIN FETCH m.createdByUser "
          + "WHERE m.clientId = :clientId "
          + "AND m.isDeleted = false "
          + "AND m.messageId IN ("
          + "  SELECT DISTINCT msg.messageId FROM Message msg "
          + "  LEFT JOIN msg.messageUserMaps mum "
          + "  LEFT JOIN msg.messageUserGroupMaps mugm "
          + "  LEFT JOIN mugm.userGroup ug "
          + "  LEFT JOIN ug.userMappings ugm "
          + "  WHERE msg.clientId = :clientId "
          + "  AND msg.isDeleted = false "
          + "  AND (mum.userId = :userId OR ugm.userId = :userId)"
          + ") "
          + "ORDER BY CASE WHEN EXISTS ("
          + "  SELECT 1 FROM MessageUserReadMap murm "
          + "  WHERE murm.messageId = m.messageId AND murm.userId = :userId"
          + ") THEN 1 ELSE 0 END, m.messageId DESC")
  Page<Message> findMessagesByUserIdPaginated(
      @Param("clientId") Long clientId, @Param("userId") Long userId, Pageable pageable);

  @Query(
      "SELECT m FROM Message m "
          + "LEFT JOIN FETCH m.createdByUser "
          + "WHERE m.messageId = :messageId AND m.clientId = :clientId AND m.isDeleted = false")
  Optional<Message> findByMessageIdAndClientId(
      @Param("messageId") Long messageId, @Param("clientId") Long clientId);

  @Query("SELECT m FROM Message m WHERE m.messageId = :messageId AND m.clientId = :clientId")
  Optional<Message> findByMessageIdAndClientIdIncludingDeleted(
      @Param("messageId") Long messageId, @Param("clientId") Long clientId);

  @Query(
      "SELECT DISTINCT m FROM Message m "
          + "LEFT JOIN m.messageUserMaps mum "
          + "LEFT JOIN m.messageUserGroupMaps mugm "
          + "WHERE m.messageId = :messageId AND m.clientId = :clientId AND m.isDeleted = false")
  Optional<Message> findByMessageIdAndClientIdWithTargets(
      @Param("messageId") Long messageId, @Param("clientId") Long clientId);

  @Query(
      "SELECT COUNT(DISTINCT m.messageId) FROM Message m "
          + "LEFT JOIN m.messageUserMaps mum "
          + "LEFT JOIN m.messageUserGroupMaps mugm "
          + "LEFT JOIN mugm.userGroup ug "
          + "LEFT JOIN ug.userMappings ugm "
          + "WHERE m.clientId = :clientId "
          + "AND m.isDeleted = false "
          + "AND (mum.userId = :userId OR ugm.userId = :userId) "
          + "AND NOT EXISTS ("
          + "  SELECT 1 FROM MessageUserReadMap murm "
          + "  WHERE murm.messageId = m.messageId AND murm.userId = :userId"
          + ")")
  long countUnreadMessagesByUserId(@Param("clientId") Long clientId, @Param("userId") Long userId);
}
