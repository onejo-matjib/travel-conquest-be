package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepositoryQueryDsl {

  Page<CouponSearchResponse> searchAllCoupons(
      Pageable pageable,
      CouponSort couponSort,
      String direction
  );
}