package com.sparta.travelconquestbe.domain.subscription.repository;

import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query(value = """
          SELECT CASE 
              WHEN NOT EXISTS (SELECT 1 FROM users WHERE id = :subUserId) THEN 'USER_NOT_FOUND'
              WHEN EXISTS (SELECT 1 FROM subscriptions WHERE user_id = :userId AND sub_user_id = :subUserId) THEN 'DUPLICATE_SUBSCRIPTION'
              ELSE 'VALID'
          END AS validation_result
          FROM dual
      """, nativeQuery = true)
  String validateSubscriptionCreation(@Param("userId") Long userId,
      @Param("subUserId") Long subUserId);
}