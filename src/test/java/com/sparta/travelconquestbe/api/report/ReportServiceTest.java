package com.sparta.travelconquestbe.api.report;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.dto.response.ReportCreateResponse;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

  @Mock
  private ReportRepository reportRepository;

  @InjectMocks
  private ReportService reportService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("신고 등록 실패 - 본인 신고")
  void createReport_SelfReport() {
    Long targetId = 1L;
    Long reporterId = 1L;

    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.SPAM)
        .build();

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(request, reporterId);
    });

    assertEquals("REPORT#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(reportRepository, never()).findLatestReportByTargetId(anyLong());
    verify(reportRepository, never()).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 실패 - 대상 없음")
  void createReport_TargetNotFound() {
    Long targetId = 1L;
    Long reporterId = 2L;

    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.ADVERTISING)
        .build();

    when(reportRepository.findLatestReportByTargetId(targetId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(request, reporterId);
    });

    assertEquals("REPORT#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(reportRepository, times(1)).findLatestReportByTargetId(targetId);
    verify(reportRepository, never()).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 실패 - 중복 신고")
  void createReport_DuplicateReport() {
    Long targetId = 1L;
    Long reporterId = 2L;

    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.ROUTE)
        .reason(Reason.PROFANITY)
        .build();

    Report latestReport = Report.builder()
        .id(1L)
        .targetId(targetId)
        .reportCategory(ReportCategory.ROUTE)
        .reason(Reason.PROFANITY)
        .status(Villain.SAINT)
        .checkedAt(null)
        .build();

    when(reportRepository.findLatestReportByTargetId(targetId)).thenReturn(Optional.of(latestReport));

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(request, reporterId);
    });

    assertEquals("REPORT#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());

    verify(reportRepository, times(1)).findLatestReportByTargetId(targetId);
    verify(reportRepository, never()).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 성공 - 최신 신고 처리 완료 후")
  void createReport_SuccessAfterProcessing() {
    Long targetId = 1L;
    Long reporterId = 2L;

    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.CHAT)
        .reason(Reason.SPAM)
        .build();

    Report latestReport = Report.builder()
        .id(1L)
        .targetId(targetId)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.ADVERTISING)
        .status(Villain.OUTLAW)
        .checkedAt(LocalDateTime.now())
        .build();

    when(reportRepository.findLatestReportByTargetId(targetId)).thenReturn(Optional.of(latestReport));
    when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
      Report report = invocation.getArgument(0);
      return Report.builder()
          .id(2L)
          .targetId(report.getTargetId())
          .reportCategory(report.getReportCategory())
          .reason(report.getReason())
          .status(Villain.OUTLAW)
          .build();
    });

    ReportCreateResponse response = reportService.createReport(request, reporterId);

    assertNotNull(response);
    assertEquals(targetId, response.getTargetId());
    assertEquals(ReportCategory.CHAT, response.getReportCategory());
    assertEquals(Reason.SPAM, response.getReason());

    verify(reportRepository, times(1)).findLatestReportByTargetId(targetId);
    verify(reportRepository, times(1)).save(any(Report.class));
  }
}
