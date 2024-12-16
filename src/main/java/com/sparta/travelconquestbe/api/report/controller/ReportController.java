package com.sparta.travelconquestbe.api.report.controller;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.dto.response.ReportCreateResponse;
import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class ReportController {

  private final ReportService reportService;

  @PostMapping("/api/users/reports")
  public ResponseEntity<ReportCreateResponse> createReport(
      @AuthUser AuthUserInfo user,
      @Valid @RequestBody ReportCreateRequest request
  ) {
    ReportCreateResponse response = reportService.createReport(user, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/api/admins/reports")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<ReportSearchResponse>> searchAllReports(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit
  ) {
    Page<ReportSearchResponse> response = reportService.searchAllReports(page, limit);
    return ResponseEntity.ok(response);
  }
}
