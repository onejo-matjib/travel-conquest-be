package com.sparta.travelconquestbe.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpAdditionalInfoRequest {
  @NotBlank(message = "사용자 이름은 필수 항목입니다.")
  private String name;

  @NotBlank(message = "사용자 생년월일은 필수 항목입니다.")
  private String birth;
}
