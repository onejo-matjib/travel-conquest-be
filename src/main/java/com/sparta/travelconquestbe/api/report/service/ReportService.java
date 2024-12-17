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

import java.time.LocalDateTime;

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

    Villain currentStatus = reportRepository.findLatestStatus(target.getId()).orElse(Villain.SAINT);

    Report report = Report.builder()
        .reporterId(reporter)
        .targetId(target)
        .reportCategory(request.getReportCategory())
        .reason(request.getReason())
        .status(currentStatus)
        .build();

    Report saved = reportRepository.save(report);

    return ReportCreateResponse.builder()
        .reportId(saved.getId())
        .reportCategory(saved.getReportCategory())
        .reason(saved.getReason())
        .targetId(saved.getTargetId().getId())
        .createdAt(saved.getCreatedAt())
        .build();
  }

  @Transactional(readOnly = true)
  public Page<ReportSearchResponse> searchAllReports(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page - 1, limit);
    return reportRepository.findAllReports(pageRequest);
  }

  // 신고 처리
  @Transactional
  public void judgeReport(Long id, boolean isGuilty, AuthUserInfo admin) {

    Report report = reportRepository.findById(id)
        .orElseThrow(() -> new CustomException("REPORT#3_001", "해당 신고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (report.getCheckedAt() != null && report.getAdminId() != null) {
      throw new CustomException("REPORT#2_002", "이미 처리된 신고입니다.", HttpStatus.CONFLICT);
    }

    if (!isGuilty) {
      // 무죄
      report.markProcessed(admin.getId());
      reportRepository.save(report);
      return;
    }

    // 유죄
    User targetUser = report.getTargetId();
    Villain currentVillain = report.getStatus();

    switch (currentVillain) {
      case SAINT -> {
        report.markProcessed(admin.getId());
        report.updateVillainStatus(Villain.OUTLAW);
        // OUTLAW: 프론트에서 "경고 상태" 모달 한 번 띄우기?
      }
      case OUTLAW -> {
        report.markProcessed(admin.getId());
        report.updateVillainStatus(Villain.DEVIL);
        // DEVIL => banUntil + 7일, 모달 메세지 띄우기
        targetUser.setBanUntil(LocalDateTime.now().plusDays(7));
        userRepository.save(targetUser);
      }
      case DEVIL -> {
        // DEVIL 상태에서 또 유죄 => 강퇴
        report.markProcessed(admin.getId());
        banUser(targetUser);
      }
      default -> {
        throw new CustomException("REPORT#2_003", "이미 처리 불가 상태거나 강퇴된 사용자 입니다.", HttpStatus.CONFLICT);
      }
    }

    reportRepository.save(report);
  }

  private void banUser(User user) {
    String deletedNickname = "delete_" + user.getNickname();
    user.changeNickname(deletedNickname);
    user.delete();
    userRepository.save(user);
  }
}
