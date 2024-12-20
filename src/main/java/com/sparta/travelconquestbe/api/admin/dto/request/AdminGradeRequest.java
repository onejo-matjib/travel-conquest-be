package com.sparta.travelconquestbe.api.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminGradeRequest {
  @NotNull(message = "신청 ID를 입력해주세요.")
  private Long requestId;

  @NotNull(message = "루트 ID를 입력해주세요.")
  private Long routeId;

  @Builder
  public AdminGradeRequest(Long requestId, Long routeId) {
    this.requestId = requestId;
    this.routeId = routeId;
  }
}
