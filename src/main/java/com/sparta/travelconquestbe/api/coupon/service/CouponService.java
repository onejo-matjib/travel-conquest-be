package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponSort;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

  private final CouponRepository couponRepository;

  public Page<CouponSearchResponse> searchAllCoupons(
      Pageable pageable,
      CouponSort couponSort,
      String direction
  ) {
    return couponRepository.searchAllCoupons(pageable, couponSort, direction);
  }
}