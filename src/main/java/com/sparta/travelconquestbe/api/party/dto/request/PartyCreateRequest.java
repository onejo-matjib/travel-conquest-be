package com.sparta.travelconquestbe.api.party.dto.request;

import com.sparta.travelconquestbe.common.annotation.PasswordRequired;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@PasswordRequired
@Getter
public class PartyCreateRequest {

  @NotBlank(message = "파티 이름은 필수입니다.")
  @Size(max = 30, message = "파티 이름은 30자를 초과할 수 없습니다.")
  private String name;

  @NotBlank(message = "파티 설명은 필수입니다.")
  @Size(max = 500, message = "파티 설명은 500자를 초과할 수 없습니다.")
  private String description;

  @NotNull(message = "최대 인원 수는 필수입니다")
  @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
  @Max(value = 30, message = "최대 인원은 30명을 초과할 수 없습니다.")
  private int countMax;

  @NotNull(message = "비밀번호 활성화 선택은 필수입니다.")
  private boolean passwordStatus;

  @Size(max = 30, message = "비밀번호는 30자를 초과할 수 없습니다.")
  private String password;

  @Size(max = 100, message = "태그는 100자를 초과할 수 없습니다.")
  private String tags; // 띄어쓰기로 구분된 태그 문자열
}