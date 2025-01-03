package com.sparta.travelconquestbe.api.user.dto.respones;

import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
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
