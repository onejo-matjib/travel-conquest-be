package com.sparta.travelconquestbe.api.user.dto.respones;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
  private Long id;
  private String name;
  private String nickname;
  private String email;
  private String birth;
  private String title;
  private int subscriptionsCount;
}
