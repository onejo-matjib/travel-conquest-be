package com.sparta.travelconquestbe.api.report.dto.response;

import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSearchResponse {

  private Long reportId;
  private Long reporterId;
  private Long targetId;
  private ReportCategory reportCategory;
  private Reason reason;
  private Villain status;
  private LocalDateTime createdAt;
  private LocalDateTime checkedAt;
  private Long adminId;

  public static ReportSearchResponse from(Report report) {
    return ReportSearchResponse.builder()
        .reportId(report.getId())
        .reporterId(report.getReporterId().getId())
        .targetId(report.getTargetId().getId())
        .reportCategory(report.getReportCategory())
        .reason(report.getReason())
        .status(report.getStatus())
        .createdAt(report.getCreatedAt())
        .checkedAt(report.getCheckedAt())
        .adminId(report.getAdminId())
        .build();
  }
}
