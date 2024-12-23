package com.sparta.travelconquestbe.api.admin.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminReportProcessRequest;
import com.sparta.travelconquestbe.api.admin.service.AdminReportService;
import com.sparta.travelconquestbe.api.admin.dto.respones.ReportSearchResponse;
import com.sparta.travelconquestbe.common.annotation.AdminUser;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins/reports")
@RequiredArgsConstructor
public class AdminReportController {

  private final AdminReportService reportService;

  @AdminUser
  @PutMapping("/approve")
  public ResponseEntity<Void> approveReport(@Valid @RequestBody AdminReportProcessRequest request, @AuthUser AuthUserInfo admin) {
    reportService.approveReport(request.getTargetId(), admin.getId(), request.getSuspensionDays());
    return ResponseEntity.noContent().build();
  }

  @AdminUser
  @PutMapping("/reject")
  public ResponseEntity<Void> rejectReport(@Valid @RequestBody AdminReportProcessRequest request, @AuthUser AuthUserInfo admin) {
    reportService.rejectReport(request.getTargetId(), admin.getId());
    return ResponseEntity.noContent().build();
  }

  @AdminUser
  @GetMapping("/api/admins/reports")
  public ResponseEntity<Page<ReportSearchResponse>> searchAllReports(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit
  ) {
    Page<ReportSearchResponse> response = reportService.searchAllReports(page, limit);
    return ResponseEntity.ok(response);
  }
}
