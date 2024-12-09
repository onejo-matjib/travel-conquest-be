package com.sparta.travelconquestbe.api.auth.dto.info;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {
  private Long id;
  private String email;
  private String nickname;
  private String providerType;

  public KakaoUserInfo(Long id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }
}