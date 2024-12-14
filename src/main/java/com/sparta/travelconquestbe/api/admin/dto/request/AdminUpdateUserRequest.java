package com.sparta.travelconquestbe.api.admin.dto.request;

import com.sparta.travelconquestbe.domain.admin.enums.AdminAction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUpdateUserRequest {

  @NotNull(message = "액션은 필수입니다.")
  private AdminAction action;
}
