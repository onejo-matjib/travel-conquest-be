package com.sparta.travelconquestbe.domain.user.repository;

import com.sparta.travelconquestbe.domain.user.entity.UserUpgradeRequest;
import com.sparta.travelconquestbe.domain.user.enums.UpgradeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUpgradeRequestRepository extends JpaRepository<UserUpgradeRequest, Long> {
  Optional<UserUpgradeRequest> findByUserId(Long userId);
  List<UserUpgradeRequest> findAllByStatus(UpgradeStatus status);

  List<UserUpgradeRequest> findByStatus(UpgradeStatus status);
}
