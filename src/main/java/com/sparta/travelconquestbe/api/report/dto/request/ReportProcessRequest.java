package com.sparta.travelconquestbe.api.report.dto.request;

import com.sparta.travelconquestbe.domain.report.enums.Villain;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportProcessRequest {
  @NotNull(message = "신고 처리 상태는 필수입니다.")
  private Villain status;
}