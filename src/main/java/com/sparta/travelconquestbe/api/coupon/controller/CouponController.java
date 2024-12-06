package com.sparta.travelconquestbe.api.coupon.controller;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSaveResponse;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.api.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<Page<CouponSearchResponse>> searchAllCoupons(
            @RequestParam(defaultValue = "1", value = "page") int page,
            @RequestParam(defaultValue = "10", value = "limit") int limit
    ) {
        Page<CouponSearchResponse> response = couponService.searchAllCoupons(page, limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<CouponSaveResponse> saveCoupon(
            @PathVariable(name = "id") Long couponId,
            @AuthenticationPrincipal AuthUser user
    ) {
        Long userId = user.getId();
        CouponSaveResponse response = couponService.saveCoupon(couponId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
