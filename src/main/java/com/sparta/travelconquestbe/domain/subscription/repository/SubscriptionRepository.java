package com.sparta.travelconquestbe.domain.subscription.repository;

import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query(value = """
      SELECT CASE
          WHEN NOT EXISTS (SELECT 1 FROM users WHERE id = :subUserId) THEN 'USER_NOT_FOUND'
          WHEN EXISTS (SELECT 1 FROM subscriptions WHERE user_id = :userId AND sub_user_id = :subUserId) THEN 'DUPLICATE_SUBSCRIPTION'
          ELSE 'VALID'
      END AS validation_result
      """, nativeQuery = true)
  String validateSubscriptionCreation(@Param("userId") Long userId,
      @Param("subUserId") Long subUserId);

  @Query(value = """
      SELECT *
      FROM subscriptions
      WHERE user_id = :userId
      AND sub_user_id = :subUserId
      """, nativeQuery = true)
  Optional<Subscription> findSubscription(@Param("userId") Long userId,
      @Param("subUserId") Long subUserId);

  @Query(value = """
      SELECT * FROM subscriptions WHERE user_id = :userId
      """, nativeQuery = true)
  Page<Subscription> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query(value = """
      SELECT * FROM subscriptions WHERE sub_user_id = :subUserId
      """, nativeQuery = true)
  Page<Subscription> findAllBySubUserId(@Param("subUserId") Long subUserId, Pageable pageable);
}
