package com.sparta.travelconquestbe.api.auth.dto.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfo {
  private String id;
  private String email;
  private String nickname;
  private String providerType;

  public void saveProviderType(String providerType) {
    this.providerType = providerType;
  }
}