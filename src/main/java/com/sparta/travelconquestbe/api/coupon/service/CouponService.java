package com.sparta.travelconquestbe.api.coupon.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

  private final CouponRepository couponRepository;

  private static final int MAX_LIMIT = 50; // 페이지 크기 최대값

  @Transactional(readOnly = true)
  public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {
    // 유효성 검증
    if (page < 1) {
      throw new CustomException("COMMON#2_001", "페이지 번호는 1 이상이어야 합니다.", BAD_REQUEST);
    }

    if (limit < 1 || limit > MAX_LIMIT) {
      throw new CustomException("COMMON#3_001", "페이지 크기는 1 이상" + MAX_LIMIT + " 이하로 설정해야 합니다.",
          BAD_REQUEST);
    }

    return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
  }
}

