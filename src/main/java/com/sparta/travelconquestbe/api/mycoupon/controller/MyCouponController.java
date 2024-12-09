package com.sparta.travelconquestbe.api.mycoupon.controller;

import com.sparta.travelconquestbe.api.mycoupon.dto.respones.MyCouponSaveResponse;
import com.sparta.travelconquestbe.api.mycoupon.service.MyCouponService;
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

    @PostMapping("/{id}/{userId}")
    public ResponseEntity<MyCouponSaveResponse> saveCoupon(
            @PathVariable(name = "id") Long couponId,
            @PathVariable Long userId
    ) {
        MyCouponSaveResponse response = myCouponService.saveCoupon(couponId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}