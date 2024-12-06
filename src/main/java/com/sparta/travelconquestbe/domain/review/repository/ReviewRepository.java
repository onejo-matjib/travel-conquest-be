package com.sparta.travelconquestbe.domain.review.repository;

import com.sparta.travelconquestbe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.user.id = :userId AND r.route.id = :routeId")
  boolean isReviewExist(@Param("userId") Long userId, @Param("routeId") Long routeId);
}
