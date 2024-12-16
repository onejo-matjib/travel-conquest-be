package com.sparta.travelconquestbe.api.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(1L)
        .reportCategory(ReportCategory.REVIEW)
        .reason(Reason.SPAM)
        .build();
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(user, request);
    });

    assertEquals("REPORT#1_001", exception.getErrorCode());
  }

  @Test
  @DisplayName("신고 등록 실패 - 중복 신고")
  void createReport_DuplicateReport() {
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long targetId = 1L;
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.ROUTE)
        .reason(Reason.PROFANITY)
        .build();
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(userRepository.getReferenceById(targetId)).thenReturn(
        User.builder().id(targetId).build());
    when(
        reportRepository.isDuplicateReport(eq(user.getId()), eq(targetId), eq("ROUTE"))).thenReturn(
        true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      reportService.createReport(user, request);
    });

    assertEquals("REPORT#2_001", exception.getErrorCode());
  }

  @Test
  @DisplayName("신고 등록 성공 - 상태 OUTLAW")
  void createReport_SuccessAfterProcessing() {
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long targetId = 1L;
    ReportCreateRequest request = ReportCreateRequest.builder()
        .targetId(targetId)
        .reportCategory(ReportCategory.CHAT)
        .reason(Reason.SPAM)
        .build();
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(userRepository.getReferenceById(targetId)).thenReturn(
        User.builder().id(targetId).build());
    when(reportRepository.isDuplicateReport(eq(user.getId()), eq(targetId), eq("CHAT"))).thenReturn(
        false);
    when(reportRepository.findLatestStatus(targetId)).thenReturn(Optional.of(Villain.OUTLAW));
    when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

    assertDoesNotThrow(() -> reportService.createReport(user, request));
  }
}
