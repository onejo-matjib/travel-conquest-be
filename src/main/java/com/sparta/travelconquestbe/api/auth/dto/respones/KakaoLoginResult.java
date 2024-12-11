package com.sparta.travelconquestbe.api.auth.dto.respones;

import com.sparta.travelconquestbe.api.auth.dto.info.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResult {
  private final boolean newUser;
  private final String token;
  private final UserInfo userInfo;

  public static KakaoLoginResult newUser(UserInfo userInfo) {
    return new KakaoLoginResult(true, null, userInfo);
  }

  public static KakaoLoginResult existingUser(String token) {
    return new KakaoLoginResult(false, token, null);
  }
}