package com.sparta.travelconquestbe.domain.review.repository;

import com.sparta.travelconquestbe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  boolean existsByUserIdAndRouteId(Long userId, Long routeId);
}
