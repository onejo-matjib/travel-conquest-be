package com.sparta.travelconquestbe.api.admin.dto.respones;

import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUpdateUserResponse {
  private Long userId;
  private String name;
  private String nickname;
  private String email;
  private String providerType;
  private String birth;
  private UserType userType;
  private Title title;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt; // 강퇴 시 삭제 일자 포함

}
