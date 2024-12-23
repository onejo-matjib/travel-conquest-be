package com.sparta.travelconquestbe.api.report.dto.response;

import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportCreateResponse {

  private Long reportId;
  private ReportCategory reportCategory;
  private Reason reason;
  private Long targetId;
}
