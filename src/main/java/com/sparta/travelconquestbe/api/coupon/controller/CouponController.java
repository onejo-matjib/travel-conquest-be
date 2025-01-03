package com.sparta.travelconquestbe.api.coupon.controller;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.api.coupon.service.CouponService;
import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponSort;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CouponController {

  private final CouponService couponService;

  @GetMapping("/coupons")
  public ResponseEntity<Page<CouponSearchResponse>> searchAllCoupons(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit,
      @ValidEnum(enumClass = CouponSort.class, message = "정렬할 컬럼값을 정확하게 입력해주세요.")
      @RequestParam(defaultValue = "VALID_UNTIL") String sort,
      @RequestParam(defaultValue = "ASC") String direction) {

    Pageable pageable = PageRequest.of(
        page - 1,
        limit);

    CouponSort couponSort = CouponSort.valueOf(sort.toUpperCase());

    return ResponseEntity.status(HttpStatus.OK)
        .body(couponService.searchAllCoupons(pageable, couponSort,
            direction));
  }
}