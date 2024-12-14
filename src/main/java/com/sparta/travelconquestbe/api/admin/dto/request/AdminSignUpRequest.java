package com.sparta.travelconquestbe.api.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSignUpRequest {

  @Email
  @NotBlank(message = "이메일을 입력해주세요.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 20, message = "최소 8글자 최대 20글자로 입력해주세요.")
  private String password;

  @NotBlank(message = "이름을 입력해주세요.")
  private String name;

  @NotBlank(message = "생년월일을 입력해주세요.")
  private String birth;

  @NotBlank(message = "닉네임을 입력해주세요")
  private String nickname;

  @Builder
  public AdminSignUpRequest(String email, String password, String name, String birth,
      String nickname) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.birth = birth;
    this.nickname = nickname;
  }
}
