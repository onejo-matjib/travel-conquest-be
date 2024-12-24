package com.sparta.travelconquestbe.config;

import com.sparta.travelconquestbe.api.notification.service.NotificationService;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuspensionScheduler {

  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Scheduled(cron = "0 0 1 * * *")
  @Transactional
  public void liftSuspensions() {
    LocalDateTime now = LocalDateTime.now();
    List<User> usersToLift = userRepository.findAllBySuspendedUntilBefore(now);

    if (!usersToLift.isEmpty()) {
      usersToLift.forEach(user -> {
        user.liftSuspension();
        notificationService.notifySuspensionLifted(user);
      });
      userRepository.saveAll(usersToLift);
      log.info("정지 해제된 사용자 수 : {}", usersToLift.size());
    } else {
      log.info("해제할 정지 사용자가 없습니다.");
    }
  }
}
