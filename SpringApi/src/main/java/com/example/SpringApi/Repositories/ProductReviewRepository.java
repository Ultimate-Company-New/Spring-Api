package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

  @Query(
      "SELECT pr FROM ProductReview pr "
          + "WHERE pr.productId IN (SELECT p.productId FROM Product p WHERE p.clientId = :clientId) "
          + "AND (:includeDeleted = true OR pr.isDeleted = false) "
          + "AND (COALESCE(:filterExpr, '') = '' OR "
          + "(CASE :columnName "
          + "WHEN 'reviewId' THEN CONCAT(pr.reviewId, '') "
          + "WHEN 'ratings' THEN CONCAT(pr.ratings, '') "
          + "WHEN 'score' THEN CONCAT(pr.score, '') "
          + "WHEN 'isDeleted' THEN CONCAT(pr.isDeleted, '') "
          + "WHEN 'review' THEN CONCAT(pr.review, '') "
          + "WHEN 'userId' THEN CONCAT(pr.userId, '') "
          + "WHEN 'productId' THEN CONCAT(pr.productId, '') "
          + "WHEN 'parentId' THEN CONCAT(pr.parentId, '') "
          + "WHEN 'createdUser' THEN CONCAT(pr.createdUser, '') "
          + "WHEN 'modifiedUser' THEN CONCAT(pr.modifiedUser, '') "
          + "WHEN 'createdAt' THEN CONCAT(pr.createdAt, '') "
          + "WHEN 'updatedAt' THEN CONCAT(pr.updatedAt, '') "
          + "WHEN 'notes' THEN CONCAT(pr.notes, '') "
          + "ELSE '' END) LIKE "
          + "(CASE :condition "
          + "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') "
          + "WHEN 'equals' THEN :filterExpr "
          + "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') "
          + "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) "
          + "WHEN 'isEmpty' THEN '' "
          + "WHEN 'isNotEmpty' THEN '%' "
          + "ELSE '' END))")
  Page<ProductReview> findPaginatedProductReviews(
      @Param("clientId") Long clientId,
      @Param("columnName") String columnName,
      @Param("condition") String condition,
      @Param("filterExpr") String filterExpr,
      @Param("includeDeleted") boolean includeDeleted,
      Pageable pageable);

  @Query(
      "SELECT pr FROM ProductReview pr "
          + "WHERE pr.reviewId = :reviewId "
          + "AND pr.productId IN (SELECT p.productId FROM Product p WHERE p.clientId = :clientId)")
  ProductReview findByReviewIdAndClientId(
      @Param("reviewId") Long reviewId, @Param("clientId") Long clientId);

  @Modifying
  @Query(
      "UPDATE ProductReview pr SET pr.isDeleted = true, pr.modifiedUser = :user WHERE pr.reviewId = :reviewId OR pr.parentId = :reviewId")
  int markAllDescendantsAsDeleted(@Param("reviewId") Long reviewId, @Param("user") String user);
}

