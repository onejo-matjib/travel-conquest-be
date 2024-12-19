package com.sparta.travelconquestbe.api.user.dto.respones;

import com.sparta.travelconquestbe.domain.user.enums.UpgradeStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpgradePendingResponse {
  private Long requestId;
  private UpgradeStatus status;
  private LocalDateTime createdAt;
  private UserUpgradePendingResponseUserInfo user;

  public UserUpgradePendingResponse(Long requestId, UpgradeStatus status, LocalDateTime createdAt, UserUpgradePendingResponseUserInfo user) {
    this.requestId = requestId;
    this.status = status;
    this.createdAt = createdAt;
    this.user = user;
  }

}
