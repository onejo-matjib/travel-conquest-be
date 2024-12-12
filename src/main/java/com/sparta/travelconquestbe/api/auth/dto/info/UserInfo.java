package com.sparta.travelconquestbe.api.auth.dto.info;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfo {
  private String id;
  private String email;
  private String nickname;
  private String providerType;

  @Builder
  public UserInfo(String id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }

  public void saveProviderType(String providerType) {
    this.providerType = providerType;
  }
}