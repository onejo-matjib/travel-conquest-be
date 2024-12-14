package com.sparta.travelconquestbe.api.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

class ReportServiceTest {

  @Mock
  private ReportRepository reportRepository;
  @Mock
  private UserRepository userRepository;
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
    ReportCreateRequest request =
        ReportCreateRequest.builder()
            .targetId(targetId)
            .reportCategory(ReportCategory.REVIEW)
            .reason(Reason.SPAM)
            .build();

    CustomException exception =
        assertThrows(
            CustomException.class,
            () -> {
              reportService.createReport(reporterId, request);
            });

    assertEquals("REPORT#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    verify(reportRepository, never()).isDuplicateReport(anyLong(), anyLong(), anyString());
    verify(reportRepository, never()).findLatestStatus(anyLong());
    verify(reportRepository, never()).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 실패 - 중복 신고")
  void createReport_DuplicateReport() {
    Long targetId = 1L;
    Long reporterId = 2L;
    ReportCreateRequest request =
        ReportCreateRequest.builder()
            .targetId(targetId)
            .reportCategory(ReportCategory.ROUTE)
            .reason(Reason.PROFANITY)
            .build();

    when(reportRepository.isDuplicateReport(eq(reporterId), eq(targetId), eq("ROUTE")))
        .thenReturn(true);

    CustomException exception =
        assertThrows(
            CustomException.class,
            () -> {
              reportService.createReport(reporterId, request);
            });

    assertEquals("REPORT#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    verify(reportRepository, times(1))
        .isDuplicateReport(eq(reporterId), eq(targetId), eq("ROUTE"));
    verify(reportRepository, never()).findLatestStatus(anyLong());
    verify(reportRepository, never()).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 성공 - 최신 신고 상태를 기반으로 신고 저장")
  void createReport_SuccessAfterProcessing() {
    Long targetId = 1L;
    Long reporterId = 2L;
    ReportCreateRequest request =
        ReportCreateRequest.builder()
            .targetId(targetId)
            .reportCategory(ReportCategory.CHAT)
            .reason(Reason.SPAM)
            .build();

    when(reportRepository.isDuplicateReport(eq(reporterId), eq(targetId), eq("CHAT")))
        .thenReturn(false);
    when(reportRepository.findLatestStatus(targetId)).thenReturn(Optional.of(Villain.OUTLAW));

    when(userRepository.getReferenceById(reporterId)).thenReturn(null);

    when(reportRepository.save(any(Report.class)))
        .thenAnswer(
            invocation -> {
              Report report = invocation.getArgument(0);
              return Report.builder()
                  .id(2L)
                  .reporterId(report.getReporterId())
                  .targetId(report.getTargetId())
                  .reportCategory(report.getReportCategory())
                  .reason(report.getReason())
                  .status(report.getStatus())
                  .build();
            });

    assertDoesNotThrow(() -> reportService.createReport(reporterId, request));
    verify(reportRepository, times(1)).isDuplicateReport(eq(reporterId), eq(targetId), eq("CHAT"));
    verify(reportRepository, times(1)).findLatestStatus(targetId);
    verify(userRepository, times(1)).getReferenceById(reporterId);
    verify(reportRepository, times(1)).save(any(Report.class));
  }

  @Test
  @DisplayName("신고 등록 실패 - 대상 아이디 NULL")
  void createReport_TargetIdNull() {
    Long reporterId = 2L;
    ReportCreateRequest request =
        ReportCreateRequest.builder().reportCategory(ReportCategory.CHAT).reason(Reason.SPAM)
            .build();

    assertDoesNotThrow(() -> reportService.createReport(reporterId, request));
    verify(reportRepository, times(1)).isDuplicateReport(eq(reporterId), eq(null), eq("CHAT"));
    verify(reportRepository, times(1)).findLatestStatus(null);
    verify(reportRepository, times(1)).save(any(Report.class));
  }
}
