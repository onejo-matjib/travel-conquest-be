package com.sparta.travelconquestbe.api.user.dto.request;

import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportCreateRequest {

  @NotNull(message = "신고 대상 ID는 필수입니다.")
  private Long targetId;

  @NotNull(message = "신고 카테고리는 필수입니다.")
  private ReportCategory reportCategory;

  @NotNull(message = "신고 사유는 필수입니다.")
  private Reason reason;
}
