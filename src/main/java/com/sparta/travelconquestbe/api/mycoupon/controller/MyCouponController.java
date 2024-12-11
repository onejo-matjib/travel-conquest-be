package com.sparta.travelconquestbe.api.mycoupon.controller;

import com.sparta.travelconquestbe.api.mycoupon.dto.respones.MyCouponSaveResponse;
import com.sparta.travelconquestbe.api.mycoupon.service.MyCouponService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mycoupons")
@RequiredArgsConstructor
public class MyCouponController {

  private final MyCouponService myCouponService;

  @PostMapping("/{couponId}")
  public ResponseEntity<MyCouponSaveResponse> saveCoupon(
      @PathVariable Long couponId,
      @AuthUser User user
  ) {
    MyCouponSaveResponse response = myCouponService.saveCoupon(couponId, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}