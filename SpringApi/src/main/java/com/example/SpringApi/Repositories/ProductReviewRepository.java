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

    Page<ProductReview> findByProductIdAndIsDeletedFalse(Long productId, Pageable pageable);

    @Modifying
    @Query("UPDATE ProductReview pr SET pr.isDeleted = true, pr.modifiedUser = :user WHERE pr.reviewId = :reviewId OR pr.parentId = :reviewId")
    int markAllDescendantsAsDeleted(@Param("reviewId") Long reviewId, @Param("user") String user);
}