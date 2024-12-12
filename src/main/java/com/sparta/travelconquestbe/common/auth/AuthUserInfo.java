package com.sparta.travelconquestbe.common.auth;

import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUserInfo {
  private Long id;
  private String name;
  private String nickname;
  private String email;
  private String providerType;
  private String birth;
  private UserType type;
  private Title title;
}
