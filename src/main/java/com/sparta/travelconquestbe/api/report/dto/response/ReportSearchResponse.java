package com.sparta.travelconquestbe.api.report.dto.response;

import com.sparta.travelconquestbe.domain.report.entity.Report;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSearchResponse {

  private Long reportId;
  private Long reporterId;
  private Long targetId;
  private String reason;
  private LocalDateTime createdAt;
  private LocalDateTime checkedAt;
  private Long adminId;

  public static ReportSearchResponse from(Report report) {
    return ReportSearchResponse.builder()
        .reportId(report.getId())
        .reporterId(report.getReporterId().getId())
        .targetId(report.getTargetId().getId())
        .reason(String.valueOf(report.getReason()))
        .createdAt(report.getCreatedAt())
        .checkedAt(report.getCheckedAt())
        .adminId(report.getAdminId())
        .build();
  }
}
