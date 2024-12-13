package com.sparta.travelconquestbe.api.report.service;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.dto.response.ReportCreateResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;

  @Transactional
  public ReportCreateResponse createReport(ReportCreateRequest request, Long reporterId) {
    if (request.getTargetId().equals(reporterId)) {
      throw new CustomException("REPORT#1_001", "본인을 신고할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    validateTarget(request.getTargetId());
    return saveReport(request);
  }

  private void validateTarget(Long targetId) {
    Report latestReport = reportRepository.findLatestReportByTargetId(targetId)
        .orElseThrow(
            () -> new CustomException("REPORT#2_001", "신고 대상이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (latestReport.getCheckedAt() == null) {
      throw new CustomException("REPORT#3_001", "처리되지 않은 동일 대상의 신고가 이미 존재합니다.",
          HttpStatus.CONFLICT);
    }
  }

  @Transactional
  public ReportCreateResponse saveReport(ReportCreateRequest request) {
    Villain currentStatus = reportRepository.findTargetStatus(request.getTargetId())
        .orElse(Villain.SAINT);

    Report report = reportRepository.save(
        Report.builder()
            .reportCategory(request.getReportCategory())
            .reason(request.getReason())
            .targetId(request.getTargetId())
            .status(currentStatus)
            .build()
    );

    return ReportCreateResponse.builder()
        .reportId(report.getId())
        .reportCategory(report.getReportCategory())
        .reason(request.getReason())
        .targetId(request.getTargetId())
        .build();
  }
}
