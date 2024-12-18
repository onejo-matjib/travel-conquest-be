package com.sparta.travelconquestbe.api.user.controller;

import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.user.dto.respones.UserUpgradePendingResponse;
import com.sparta.travelconquestbe.api.user.service.UserUpgradeRequestService;
import com.sparta.travelconquestbe.common.annotation.AdminUser;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.user.entity.UserUpgradeRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class UserUpgradeRequestController {
  private final UserUpgradeRequestService upgradeRequestService;

  // 일반 유저가 등급업 신청
  @PostMapping("/users/upgrade")
  public ResponseEntity<Long> createUpgradeRequest(@AuthUser AuthUserInfo user,
      @RequestBody RouteCreateRequest routeCreateRequest) {
    Long requestId = upgradeRequestService.createUpgradeRequest(user, routeCreateRequest);
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
  @PostMapping("/admins/upgrade-requests/{requestId}/approve")
  public ResponseEntity<Void> approveRequest(@PathVariable Long requestId, @AuthUser AuthUserInfo admin) {
    upgradeRequestService.approveRequest(requestId, admin.getId());
    return ResponseEntity.noContent().build();
  }

  // 관리자 거절
  @AdminUser
  @PostMapping("/admins/upgrade-requests/{requestId}/reject")
  public ResponseEntity<Void> rejectRequest(@PathVariable Long requestId, @AuthUser AuthUserInfo admin) {
    upgradeRequestService.rejectRequest(requestId, admin.getId());
    return ResponseEntity.noContent().build();
  }
}
