package com.sparta.travelconquestbe.domain.review.repository;

import com.sparta.travelconquestbe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query(value = """
          SELECT CASE 
              WHEN NOT EXISTS (SELECT 1 FROM routes WHERE id = :routeId) THEN 'ROUTE_NOT_FOUND'
              WHEN NOT EXISTS (SELECT 1 FROM users WHERE id = :userId) THEN 'USER_NOT_FOUND'
              WHEN EXISTS (SELECT 1 FROM reviews WHERE user_id = :userId AND route_id = :routeId) THEN 'DUPLICATE_REVIEW'
              ELSE 'VALID'
          END AS validation_result
          FROM dual
      """, nativeQuery = true)
  String validateReviewCreation(@Param("userId") Long userId, @Param("routeId") Long routeId);
}
