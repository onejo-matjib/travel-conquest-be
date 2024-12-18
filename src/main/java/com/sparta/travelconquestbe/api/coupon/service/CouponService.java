package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

  private final CouponRepository couponRepository;

  public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {

    return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
  }
}

