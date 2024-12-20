package com.sparta.travelconquestbe.api.report.service;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.dto.response.ReportCreateResponse;
import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;

  @Transactional
  public ReportCreateResponse createReport(AuthUserInfo user, ReportCreateRequest request) {
    User reporter = userRepository.getReferenceById(user.getId());
    User target = userRepository.getReferenceById(request.getTargetId());

    if (reporter.getId().equals(target.getId())) {
      throw new CustomException("REPORT#1_001", "본인을 신고할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    boolean isDuplicate = reportRepository.isDuplicateReport(
        reporter.getId(), target.getId(), request.getReportCategory().name());
    if (isDuplicate) {
      throw new CustomException("REPORT#2_001", "이미 신고가 처리 중입니다.", HttpStatus.CONFLICT);
    }

    Villain currentStatus = getCurrentVillainStatus(target.getId());
    Report report = saveReport(reporter, target, request, currentStatus);

    return ReportCreateResponse.builder()
        .reportId(report.getId())
        .reportCategory(report.getReportCategory())
        .reason(report.getReason())
        .targetId(report.getTargetId().getId())
        .createdAt(report.getCreatedAt())
        .build();
  }

  @Transactional(readOnly = true)
  public Villain getCurrentVillainStatus(Long targetId) {
    return reportRepository.findLatestStatus(targetId).orElse(Villain.SAINT);
  }

  @Transactional
  public Report saveReport(User reporter, User target, ReportCreateRequest request,
      Villain currentStatus) {
    Report report = Report.builder()
        .reporterId(reporter)
        .targetId(target)
        .reportCategory(request.getReportCategory())
        .reason(request.getReason())
        .status(currentStatus)
        .build();

    return reportRepository.save(report);
  }

}
