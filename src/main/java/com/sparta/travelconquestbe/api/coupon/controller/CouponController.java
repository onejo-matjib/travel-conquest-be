package com.sparta.travelconquestbe.api.coupon.controller;

import com.sparta.travelconquestbe.api.coupon.dto.request.CouponCreateRequest;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponCreateResponse;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.api.coupon.service.CouponService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Validated
public class CouponController {

  private final CouponService couponService;

  @GetMapping
  public ResponseEntity<Page<CouponSearchResponse>> searchAllCoupons(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit
  ) {
    Page<CouponSearchResponse> response = couponService.searchAllCoupons(page, limit);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/admins")
  public ResponseEntity<CouponCreateResponse> createCoupon(
      @Valid @RequestBody CouponCreateRequest request,
      @AuthUser AuthUserInfo user
  ) {
    CouponCreateResponse response = couponService.createCoupon(request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}/admins")
  public ResponseEntity<String> deleteCoupon(
      @PathVariable Long id,
      @AuthUser AuthUserInfo user
  ) {
    couponService.deleteCounpon(id, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("해당 쿠폰이 삭제되었습니다.");
  }
}
