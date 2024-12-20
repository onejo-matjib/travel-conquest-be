package com.sparta.travelconquestbe.api.user.dto.respones;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpgradePendingResponseUserInfo {
  private Long userId;
  private String username;
  private String email;

  public UserUpgradePendingResponseUserInfo(Long userId, String username, String email) {
    this.userId = userId;
    this.username = username;
    this.email = email;
  }
}
