package com.sparta.travelconquestbe.api.admin.controller;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminSignUpRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminUpdateUserRequest;
import com.sparta.travelconquestbe.api.admin.dto.respones.AdminUpdateUserResponse;
import com.sparta.travelconquestbe.api.admin.service.AdminService;
import com.sparta.travelconquestbe.common.annotation.AdminUser;
import com.sparta.travelconquestbe.api.coupon.dto.request.CouponCreateRequest;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponCreateResponse;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.admin.enums.AdminAction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @AdminUser
  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody AdminSignUpRequest request) {
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

  @AdminUser
  @PutMapping("/users/{userId}")
  public ResponseEntity<AdminUpdateUserResponse> updateUser(@PathVariable long userId,
      @Valid @RequestBody AdminUpdateUserRequest updateRequest) {

    AdminAction action = updateRequest.getAction();
    AdminUpdateUserResponse response;

    if (action == AdminAction.BAN) {
      response = adminService.banUser(userId);
    } else if (action == AdminAction.UPDATE) {
      response = adminService.updateUserLevel(userId);
    } else if (action == AdminAction.RESTORE) {
      adminService.restoreUser(userId);
      return ResponseEntity.ok().build();
  } else {
      throw new CustomException("ADMIN#5_001", "올바르지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(response);
  }

  @PostMapping("/coupons")
  public ResponseEntity<CouponCreateResponse> createCoupon(
      @Valid @RequestBody CouponCreateRequest request,
      @AuthUser AuthUserInfo user
  ) {
    CouponCreateResponse response = adminService.createCoupon(request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/coupons/{id}")
  public ResponseEntity<String> deleteCoupon(
      @PathVariable Long id,
      @AuthUser AuthUserInfo user
  ) {
    adminService.deleteCoupon(id, user);
    return ResponseEntity.noContent().build();
  }
}
