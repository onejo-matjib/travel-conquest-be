package com.sparta.travelconquestbe.api.report.service;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;

  @Transactional
  public void createReport(Long reporterId, ReportCreateRequest request) {
    if (reporterId.equals(request.getTargetId())) {
      throw new CustomException("REPORT#1_001", "본인을 신고할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    boolean isDuplicate =
        reportRepository.isDuplicateReport(
            reporterId, request.getTargetId(), request.getReportCategory().name());
    if (isDuplicate) {
      throw new CustomException("REPORT#2_001", "이미 신고한 대상입니다.", HttpStatus.BAD_REQUEST);
    }
    Villain currentStatus = getCurrentVillainStatus(request.getTargetId());
    saveReport(reporterId, request, currentStatus);
  }

  @Transactional(readOnly = true)
  public Villain getCurrentVillainStatus(Long targetId) {
    return reportRepository.findLatestStatus(targetId).orElse(Villain.SAINT);
  }

  @Transactional
  public void saveReport(Long reporterId, ReportCreateRequest request, Villain currentStatus) {
    userRepository.getReferenceById(reporterId);
    Report report =
        Report.builder()
            .reporterId(reporterId)
            .targetId(request.getTargetId())
            .reportCategory(request.getReportCategory())
            .reason(request.getReason())
            .status(currentStatus)
            .build();
    reportRepository.save(report);
  }
}
