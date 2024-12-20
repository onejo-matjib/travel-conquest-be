package com.sparta.travelconquestbe.api.user.service;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminGradeRequest;
import com.sparta.travelconquestbe.api.route.service.RouteService;
import com.sparta.travelconquestbe.api.user.dto.respones.UserUpgradePendingResponse;
import com.sparta.travelconquestbe.api.user.dto.respones.UserUpgradePendingResponseUserInfo;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.enums.RouteStatus;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.entity.UserUpgradeRequest;
import com.sparta.travelconquestbe.domain.user.enums.UpgradeStatus;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import com.sparta.travelconquestbe.domain.user.repository.UserUpgradeRequestRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpgradeRequestService {
  private final UserUpgradeRequestRepository upgradeRequestRepository;
  private final UserRepository userRepository;
  private final RouteService routeService;
  private final RouteRepository routeRepository;

  @Transactional
  public Long createUpgradeRequest(AuthUserInfo userInfo, Long routeId) {
    User user =
        userRepository
            .findById(userInfo.getId())
            .orElseThrow(
                () -> new CustomException("USER#3_003", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (user.getType() != UserType.USER) {
      throw new CustomException(
          "UPGRADE#1_001", "이미 등급업 신청을 하였거나 등급업 된 유저입니다.", HttpStatus.BAD_REQUEST);
    }

    upgradeRequestRepository
        .findByUserId(user.getId())
        .ifPresent(
            req -> {
              if (req.isPending()) {
                throw new CustomException(
                    "UPGRADE#2_001", "이미 등급업 심사 대기 중입니다.", HttpStatus.CONFLICT);
              }
            });

    Route route =
        routeRepository
            .findByUnauthorizedRoute(routeId)
            .orElseThrow(
                () ->
                    new CustomException("UPGRADE#3_001", "해당 루트를 찾을수 없습니다.", HttpStatus.NOT_FOUND));

    UserUpgradeRequest upgradeRequest =
        UserUpgradeRequest.builder().user(user).route(route).status(UpgradeStatus.PENDING).build();

    UserUpgradeRequest saved = upgradeRequestRepository.save(upgradeRequest);
    return saved.getId();
  }

  @Transactional(readOnly = true)
  public List<UserUpgradePendingResponse> getPendingRequests() {
    List<UserUpgradeRequest> requests =
        upgradeRequestRepository.findByStatus(UpgradeStatus.PENDING);
    return requests.stream()
        .map(
            req ->
                new UserUpgradePendingResponse(
                    req.getId(),
                    req.getStatus(),
                    req.getCreatedAt(),
                    new UserUpgradePendingResponseUserInfo(
                        req.getUser().getId(),
                        req.getUser().getNickname(),
                        req.getUser().getEmail())))
        .collect(Collectors.toList());
  }

  @Transactional
  public void approveRequest(AdminGradeRequest adminGradeRequest, Long adminId) {
    UserUpgradeRequest request =
        upgradeRequestRepository
            .findById(adminGradeRequest.getRequestId())
            .orElseThrow(
                () ->
                    new CustomException(
                        "UPGRADE#3_002", "해당 등급업 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (request.getStatus() != UpgradeStatus.PENDING) {
      throw new CustomException("UPGRADE#2_002", "이미 처리된 요청입니다.", HttpStatus.CONFLICT);
    }

    // 승인 처리
    request.approve();

    User user = request.getUser();
    if (user.getType() == UserType.USER) {
      user.updateUserType();
      userRepository.save(user);
    }
    Route route =
        routeRepository
            .findByUnauthorizedRoute(adminGradeRequest.getRouteId())
            .orElseThrow(
                () ->
                    new CustomException(
                        "UPGRADE#3_003", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    route.setStatus(RouteStatus.AUTHORIZED);
    routeRepository.save(request.getRoute());
  }

  @Transactional
  public void rejectRequest(AdminGradeRequest adminGradeRequest, AuthUserInfo admin) {
    UserUpgradeRequest request =
        upgradeRequestRepository
            .findById(adminGradeRequest.getRequestId())
            .orElseThrow(
                () ->
                    new CustomException(
                        "UPGRADE#3_004", "해당 등급업 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (request.getStatus() != UpgradeStatus.PENDING) {
      throw new CustomException("UPGRADE#2_003", "이미 처리된 요청입니다.", HttpStatus.CONFLICT);
    }
    Route route =
        routeRepository
            .findByUnauthorizedRoute(adminGradeRequest.getRouteId())
            .orElseThrow(
                () ->
                    new CustomException(
                        "UPGRADE#3_005", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    upgradeRequestRepository.delete(request);
    routeService.routeDelete(route.getId(), admin);
  }
}
