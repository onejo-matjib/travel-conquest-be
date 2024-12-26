package com.sparta.travelconquestbe.api.mycoupon.controller;

import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponListResponse;
import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponSaveResponse;
import com.sparta.travelconquestbe.api.mycoupon.service.MyCouponService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MyCouponController {

  private final MyCouponService myCouponService;

  @PostMapping("/mycoupons/{couponId}")
  public ResponseEntity<MyCouponSaveResponse> createMyCoupon(
      @PathVariable Long couponId,
      @AuthUser AuthUserInfo user
  ) {
    MyCouponSaveResponse response = myCouponService.createMyCoupon(couponId, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/users/mycoupons")
  public ResponseEntity<Page<MyCouponListResponse>> searchAllMyCoupons(
      @AuthUser AuthUserInfo userInfo,
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit,
      @ValidEnum(enumClass = CouponSort.class, message = "정렬할 컬럼값을 정확하게 입력해주세요.")
      @RequestParam(defaultValue = "VALID_UNTIL") String sort,
      @RequestParam(defaultValue = "ASC") String direction) {

    if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
      throw new CustomException("PARTY#1_002",
          "정렬 방향은 ASC 또는 DESC만 가능합니다.",
          HttpStatus.BAD_REQUEST);
    }

    Pageable pageable = PageRequest.of(
        page - 1,
        limit);

    CouponSort couponSort = CouponSort.valueOf(sort.toUpperCase());

    return ResponseEntity.status(HttpStatus.OK).body(myCouponService.searchAllMyCoupons(userInfo,
        pageable,
        couponSort,
        direction));
  }
}