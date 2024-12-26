package com.sparta.travelconquestbe.api.party.dto.request;

import com.sparta.travelconquestbe.common.annotation.PasswordRequired;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

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
  @Range(min = 1, max = 30, message = "최소 인원은 1명, 최대 인원은 30명입니다.")
  private int countMax;

  @NotNull(message = "비밀번호 활성화 선택은 필수입니다.")
  private boolean passwordStatus;

  @Size(max = 30, message = "비밀번호는 30자를 초과할 수 없습니다.")
  private String password;

  @Size(max = 100, message = "태그는 100자를 초과할 수 없습니다.")
  private String tags; // 띄어쓰기로 구분된 태그 문자열

  public PartyCreateRequest(String name,
      String description,
      int countMax,
      boolean passwordStatus,
      String password,
      String tags
  ) {
    this.name = name;
    this.description = description;
    this.countMax = countMax;
    this.passwordStatus = passwordStatus;
    this.password = password;
    this.tags = tags;
  }
}