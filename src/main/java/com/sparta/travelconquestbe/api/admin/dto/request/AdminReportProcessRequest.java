package com.sparta.travelconquestbe.api.admin.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReportProcessRequest {

  @NotNull(message = "targetId는 필수입니다.")
  private Long targetId;

  @NotNull(message = "suspensionDays는 필수입니다.")
  @Min(value = 1, message = "정지 일수는 최소 1일 이상이어야 합니다.")
  private Integer suspensionDays;

  @Builder
  public AdminReportProcessRequest(Long targetId, Integer suspensionDays) {
    this.targetId = targetId;
    this.suspensionDays = suspensionDays;
  }

}
