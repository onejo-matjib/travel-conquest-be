package com.sparta.travelconquestbe.domain.user.repository;

import com.sparta.travelconquestbe.api.user.dto.respones.UserRankingResponse;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
  
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByEmail(String email);

    @Query("SELECT new com.sparta.travelconquestbe.api.user.dto.respones.UserRankingResponse(" +
        "u.id, u.nickname, u.subscriptionCount, u.title) " +
        "FROM User u WHERE u.type <> 'ADMIN' ORDER BY u.subscriptionCount DESC")
    List<UserRankingResponse> findTop100UsersBySubscriptions(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.subscriptionCount >= 100000 AND u.title <> 'CONQUEROR'")
    List<User> findUsersToUpdateTitle();

    @Query("SELECT u FROM User u WHERE u.suspendedUntil <= :now AND u.suspendedUntil IS NOT NULL")
    List<User> findAllBySuspendedUntilBefore(LocalDateTime now);
}