package com.sparta.travelconquestbe.domain.mycoupon.repository;

import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyCouponRepositoryQueryDsl {

  Page<MyCouponListResponse> searchAllMyCoupons(Long userId, Pageable pageable);
}