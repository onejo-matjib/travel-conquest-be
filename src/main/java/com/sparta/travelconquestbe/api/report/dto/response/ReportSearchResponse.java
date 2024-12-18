package com.sparta.travelconquestbe.api.report.dto.response;

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

  public ReportSearchResponse(Long reportId, Long reporterId, Long targetId,
      ReportCategory reportCategory, Reason reason, Villain status,
      LocalDateTime createdAt, LocalDateTime checkedAt, Long adminId) {
    this.reportId = reportId;
    this.reporterId = reporterId;
    this.targetId = targetId;
    this.reportCategory = reportCategory;
    this.reason = reason;
    this.status = status;
    this.createdAt = createdAt;
    this.checkedAt = checkedAt;
    this.adminId = adminId;
  }
}
