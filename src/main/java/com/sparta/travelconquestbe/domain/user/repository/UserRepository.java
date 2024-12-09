package com.sparta.travelconquestbe.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderId(Long providerId);
  Optional<User> findByEmail(String email);
}
