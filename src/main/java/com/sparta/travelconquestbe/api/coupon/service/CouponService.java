package com.sparta.travelconquestbe.api.coupon.service;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.sparta.travelconquestbe.api.coupon.dto.request.CouponCreateRequest;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponCreateResponse;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

  private final CouponRepository couponRepository;
  private final UserRepository userRepository;
  private final Clock clock;

  public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {

    return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
  }

  public CouponCreateResponse createCoupon(CouponCreateRequest request, Long userId) {

    //유저가 관리자인지 확인(임시)
    validateUser(userId);

    Coupon coupon = getCoupon(request);

    couponRepository.save(coupon);

    return buildResponse(coupon);
  }


  //유저가 관리자인지 확인(임시)
  public void validateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다"));

    if (!(user.getType().equals(UserType.ADMIN))) {
      throw new CustomException("COUPON#1_002",
          "해당 리소스에 접근할 권한이 없습니다.",
          FORBIDDEN);
    }
  }


  // 쿠폰 엔티티 생성
  private Coupon getCoupon(CouponCreateRequest request) {
    Coupon coupon = Coupon.builder()
        .name(request.getName())
        .description(request.getDescription())
        .type(request.getType())
        .code(request.getCode())
        .discountAmount(request.getDiscountAmount())
        .validUntil(request.getValidUntil())
        .count(request.getCount())
        .build();
    return coupon;
  }

  // 응답 생성
  private CouponCreateResponse buildResponse(Coupon coupon) {

    return CouponCreateResponse.builder()
        .id(coupon.getId())
        .name(coupon.getName())
        .descriotion(coupon.getDescription())
        .type(coupon.getType())
        .code(coupon.getCode())
        .discountAmount(coupon.getDiscountAmount())
        .validUntil(coupon.getValidUntil())
        .count(coupon.getCount())
        .createdAt(coupon.getCreatedAt())
        .updatedAt(coupon.getUpdatedAt())
        .build();
  }
}

