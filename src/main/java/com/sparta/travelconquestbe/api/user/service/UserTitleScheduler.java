package com.sparta.travelconquestbe.api.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.sparta.travelconquestbe.api.user.dto.respones.UserRankingResponse;

@Component
@RequiredArgsConstructor
public class UserTitleScheduler {

  private final UserService userService;

  @Scheduled(cron = "0 0 * * * *") // 매 정각 실행
  public void updateTitlesForEligibleUsers() {
    userService.updateTitlesForEligibleUsers();
  }
}
