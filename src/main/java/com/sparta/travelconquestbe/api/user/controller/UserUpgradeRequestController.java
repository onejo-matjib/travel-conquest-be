package com.sparta.travelconquestbe.api.user.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminGradeRequest;
import com.sparta.travelconquestbe.api.user.dto.respones.UserUpgradePendingResponse;
import com.sparta.travelconquestbe.api.user.service.UserUpgradeRequestService;
import com.sparta.travelconquestbe.common.annotation.AdminUser;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserUpgradeRequestController {
  private final UserUpgradeRequestService upgradeRequestService;

  // 일반 유저가 등급업 신청
  @PostMapping("/users/upgrade/{routeId}")
  public ResponseEntity<Long> createUpgradeRequest(
      @AuthUser AuthUserInfo user, @PathVariable Long routeId) {
    Long requestId = upgradeRequestService.createUpgradeRequest(user, routeId);
    return ResponseEntity.status(HttpStatus.CREATED).body(requestId);
  }

  // 관리자 등급업 신청 대기 목록 조회
  @AdminUser
  @GetMapping("/admins/upgrade-requests")
  public ResponseEntity<List<UserUpgradePendingResponse>> getPendingRequests() {
    List<UserUpgradePendingResponse> requests = upgradeRequestService.getPendingRequests();
    return ResponseEntity.ok(requests);
  }

  // 관리자 승인
  @AdminUser
  @PutMapping("/admins/upgrade-requests/approve")
  public ResponseEntity<Void> approveRequest(
          @Valid @RequestBody AdminGradeRequest adminGradeRequest, @AuthUser AuthUserInfo admin) {
    upgradeRequestService.approveRequest(adminGradeRequest, admin.getId());
    return ResponseEntity.noContent().build();
  }

  // 관리자 거절
  @AdminUser
  @DeleteMapping("/admins/upgrade-requests/reject")
  public ResponseEntity<Void> rejectRequest(
          @Valid @RequestBody AdminGradeRequest adminGradeRequest, @AuthUser AuthUserInfo admin) {
    upgradeRequestService.rejectRequest(adminGradeRequest, admin);
    return ResponseEntity.noContent().build();
  }
}
