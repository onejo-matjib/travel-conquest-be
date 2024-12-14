package com.sparta.travelconquestbe.api.admin.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminSignUpRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminUpdateUserRequest;
import com.sparta.travelconquestbe.api.admin.dto.respones.AdminUpdateUserResponse;
import com.sparta.travelconquestbe.api.admin.service.AdminService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.admin.enums.AdminAction;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody AdminSignUpRequest request,
      @AuthUser AuthUserInfo user) {

    if (!UserType.ADMIN.equals(user.getType())) {
      throw new CustomException("ADMIN#2_001", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    adminService.signUp(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody AdminLoginRequest request) {
    String token = adminService.login(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<AdminUpdateUserResponse> updateUser(@AuthUser AuthUserInfo admin, @PathVariable long userId,
      @Valid @RequestBody AdminUpdateUserRequest updateRequest) {

    AdminAction action = updateRequest.getAction();
    AdminUpdateUserResponse response;

    if (action == AdminAction.BAN) {
      response = adminService.banUser(admin, userId);
    } else if (action == AdminAction.UPDATE) {
      response = adminService.updateUserLevel(admin, userId);
    } else {
      throw new CustomException("ADMIN#5_001", "올바르지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(response);
  }

}
