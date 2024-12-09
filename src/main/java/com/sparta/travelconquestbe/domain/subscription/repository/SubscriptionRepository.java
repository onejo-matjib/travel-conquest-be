package com.sparta.travelconquestbe.domain.subscription.repository;

import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query("SELECT EXISTS (SELECT 1 FROM Subscription s WHERE s.userId = :userId AND s.subUserId = :subUserId)")
  boolean isSubscribed(@Param("userId") Long userId, @Param("subUserId") Long subUserId);
}
