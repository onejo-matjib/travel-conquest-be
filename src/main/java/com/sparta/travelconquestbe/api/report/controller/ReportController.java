package com.sparta.travelconquestbe.api.report.controller;

import com.sparta.travelconquestbe.api.report.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.report.service.ReportService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PostMapping("/api/users/reports")
  public ResponseEntity<Void> createReport(
      @AuthUser AuthUserInfo user,
      @Valid @RequestBody ReportCreateRequest request
  ) {
    reportService.createReport(user, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
