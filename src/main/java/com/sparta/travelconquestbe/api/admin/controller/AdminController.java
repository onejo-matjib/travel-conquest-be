package com.sparta.travelconquestbe.api.admin.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.service.AdminService;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody AuthSignUpRequest request,
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

}
