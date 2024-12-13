package com.sparta.travelconquestbe.api.report.dto.request;

import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {

  @NotNull(message = "신고 대상 ID는 필수입니다.")
  private Long targetId;

  @NotNull(message = "신고 카테고리는 필수입니다.")
  @ValidEnum(enumClass = ReportCategory.class, message = "유효하지 않은 카테고리입니다.")
  private ReportCategory reportCategory;

  @NotNull(message = "신고 사유는 필수입니다.")
  @ValidEnum(enumClass = Reason.class, message = "유효하지 않은 신고 사유입니다.")
  private Reason reason;

  @Builder
  public ReportCreateRequest(Long targetId, ReportCategory reportCategory, Reason reason) {
    this.targetId = targetId;
    this.reportCategory = reportCategory;
    this.reason = reason;
  }
}
