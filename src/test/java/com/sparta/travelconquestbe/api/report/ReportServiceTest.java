package com.sparta.travelconquestbe.api.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.dto.response.ReportCreateResponse;
import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
  @DisplayName("신고 성공")
  void createReport_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "email", "name", "nick", "avatar", "bio", null, null);
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(2L)
        .reportCategory(ReportCategory.CHAT)
        .reason(Reason.SPAM)
        .build();

    User reporter = User.builder().id(user.getId()).build();
    User target = User.builder().id(request.getTargetId()).build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(reporter);
    when(userRepository.getReferenceById(request.getTargetId())).thenReturn(target);
    when(
        reportRepository.isDuplicateReport(user.getId(), request.getTargetId(), "CHAT")).thenReturn(
        false);

    Report savedReport = Report.builder()
        .id(1L)
        .reporterId(reporter)
        .targetId(target)
        .reportCategory(request.getReportCategory())
        .reason(request.getReason())
        .status(Villain.SAINT)
        .build();

    when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

    ReportCreateResponse response = reportService.createReport(user, request);

    assertNotNull(response);
    assertEquals(1L, response.getReportId());
    assertEquals(Reason.SPAM, response.getReason());
  }

  @Test
  @DisplayName("신고 실패 - 자기 자신 신고")
  void createReport_FailSelfReport() {
    AuthUserInfo user = new AuthUserInfo(1L, "email", "name", "nick", "avatar", "bio", null, null);
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(1L)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.HATE)
        .build();

    User reporter = User.builder().id(user.getId()).build();
    when(userRepository.getReferenceById(user.getId())).thenReturn(reporter);

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(user, request);
    });

    assertEquals("REPORT#1_001", exception.getErrorCode());
    assertEquals("본인을 신고할 수 없습니다.", exception.getErrorMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }

  @Test
  @DisplayName("신고 실패 - 중복 신고")
  void createReport_FailDuplicateReport() {
    AuthUserInfo user = new AuthUserInfo(1L, "email", "name", "nick", "avatar", "bio", null, null);
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(2L)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.SPAM)
        .build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(userRepository.getReferenceById(request.getTargetId())).thenReturn(
        User.builder().id(request.getTargetId()).build());
    when(reportRepository.isDuplicateReport(user.getId(), request.getTargetId(),
        "REVIEW")).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(user, request);
    });

    assertEquals("REPORT#2_001", exception.getErrorCode());
    assertEquals("이미 신고가 처리 중입니다.", exception.getErrorMessage());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
  }

  @Test
  @DisplayName("모든 신고 목록 조회 성공")
  void searchAllReports_Success() {
    PageRequest pageable = PageRequest.of(0, 10);
    Report report = Report.builder()
        .id(1L)
        .reporterId(User.builder().id(1L).build())
        .targetId(User.builder().id(2L).build())
        .reportCategory(ReportCategory.ROUTE)
        .reason(Reason.SPAM)
        .status(Villain.OUTLAW)
        .checkedAt(LocalDateTime.now())
        .adminId(3L)
        .build();

    Page<Report> reportPage = new PageImpl<>(List.of(report), pageable, 1);

    when(reportRepository.findAllReports(pageable)).thenReturn(reportPage);

    Page<ReportSearchResponse> response = reportService.searchAllReports(1, 10);

    assertNotNull(response);
    assertEquals(1, response.getContent().size());
    assertEquals(ReportCategory.ROUTE, response.getContent().get(0).getReportCategory());
    assertEquals(Villain.OUTLAW, response.getContent().get(0).getStatus());
  }

  @Test
  @DisplayName("모든 신고 목록 조회 성공 - 빈 결과")
  void searchAllReports_EmptyResult() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<Report> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(reportRepository.findAllReports(pageable)).thenReturn(emptyPage);

    Page<ReportSearchResponse> response = reportService.searchAllReports(1, 10);

    assertNotNull(response);
    assertEquals(0, response.getTotalElements());
    assertEquals(0, response.getContent().size());
  }
}
