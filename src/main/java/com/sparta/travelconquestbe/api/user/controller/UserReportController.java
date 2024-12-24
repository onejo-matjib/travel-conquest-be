package com.sparta.travelconquestbe.api.user.controller;

import com.sparta.travelconquestbe.api.user.dto.request.ReportCreateRequest;
import com.sparta.travelconquestbe.api.user.dto.respones.ReportCreateResponse;
import com.sparta.travelconquestbe.api.user.service.UserReportService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UserReportController {

  private final UserReportService userReportService;

  @PostMapping("/api/users/reports")
  public ResponseEntity<ReportCreateResponse> createReport(
      @AuthUser AuthUserInfo user,
      @Valid @RequestBody ReportCreateRequest request
  ) {
    ReportCreateResponse response = userReportService.createReport(user, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
