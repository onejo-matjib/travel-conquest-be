package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.api.notification.service.NotificationService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Transactional
  public void approveReport(Long targetId, Long adminId, Integer suspensionDays) {
    User targetUser = userRepository.findById(targetId)
        .orElseThrow(() -> new CustomException("REPORT#3_001", "대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    List<Report> relatedReports = reportRepository.findAllByTargetIdAndCheckedAtIsNull(targetUser);

    if (relatedReports.isEmpty()) {
      throw new CustomException("REPORT#3_002", "처리할 신고가 없습니다.", HttpStatus.NOT_FOUND);
    }

    for (Report report : relatedReports) {
      report.markProcessed(adminId);
    }
    reportRepository.saveAll(relatedReports);
    targetUser.suspendUser(suspensionDays);
    userRepository.save(targetUser);
    notificationService.notifyUserSuspended(targetUser, suspensionDays);
  }

  @Transactional
  public void rejectReport(Long targetId, Long adminId) {
    User targetUser = userRepository.findById(targetId)
        .orElseThrow(() -> new CustomException("REPORT#3_003", "대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    List<Report> relatedReports = reportRepository.findAllByTargetIdAndCheckedAtIsNull(targetUser);

    if (relatedReports.isEmpty()) {
      throw new CustomException("REPORT#3_004", "처리할 신고가 없습니다.", HttpStatus.NOT_FOUND);
    }

    for (Report report : relatedReports) {
      report.markProcessed(adminId);
    }
    reportRepository.saveAll(relatedReports);
  }
}
