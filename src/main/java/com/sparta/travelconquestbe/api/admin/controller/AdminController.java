package com.sparta.travelconquestbe.api.admin.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.service.AdminService;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
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
  public ResponseEntity<Void> signUp(@RequestBody AuthSignUpRequest request) {
    adminService.signUp(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request) {
    String token = adminService.login(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
  }

}
