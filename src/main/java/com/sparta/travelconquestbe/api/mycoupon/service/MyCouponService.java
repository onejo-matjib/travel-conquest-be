package com.sparta.travelconquestbe.api.mycoupon.service;

import com.sparta.travelconquestbe.api.mycoupon.dto.respones.MyCouponSaveResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyCouponService {

  private final MyCouponRepository myCouponRepository;
  private final CouponRepository couponRepository;
  private final Clock clock;
  private final UserRepository userRepository;

  // 쿠폰 저장
  public MyCouponSaveResponse createMyCoupon(Long couponId, AuthUserInfo userInfo) {

    validateUser(userInfo);
    Coupon coupon = getCouponById(couponId);
    validateCoupon(coupon, userInfo);

    // 쿠폰 코드 발급
    String couponCode = UUID.randomUUID().toString();

    // 유저 프록시 객체
    User referenceUser = getUserById(userInfo.getId());

    // 쿠폰 저장
    MyCoupon myCoupon = MyCoupon.builder()
        .code(couponCode)
        .status(UseStatus.AVAILABLE)
        .user(referenceUser)
        .coupon(coupon)
        .build();
    coupon.decrementCount();
    myCouponRepository.save(myCoupon);

    return buildResponse(myCoupon);
  }

  // 유저 권한 확인
  public void validateUser(AuthUserInfo userInfo) {
    if (userInfo.getType().equals(UserType.USER)) {
      throw new CustomException("COUPON#3_002",
          "인증된 사용자가 아닙니다.",
          HttpStatus.FORBIDDEN);
    }
  }

  // 쿠폰 유효성 검사
  public void validateCoupon(Coupon coupon, AuthUserInfo userInfo) {
    if (myCouponRepository.existsByCouponIdAndUserIdAndStatus(
        coupon.getId(), userInfo.getId(), UseStatus.AVAILABLE)) {
      throw new CustomException("COUPON#4_001",
          "중복된 사용 가능한 쿠폰이 존재합니다.",
          HttpStatus.CONFLICT);
    }

    if (coupon.getCount() <= 0) {
      throw new CustomException("COUPON#4_002",
          "해당 쿠폰이 소진되었습니다.",
          HttpStatus.CONFLICT);
    }

    checkCouponExpiration(coupon);

    if (coupon.getType().equals(CouponType.PREMIUM)
        && !userInfo.getTitle().equals(Title.CONQUEROR)) {
      throw new CustomException("COUPON#4_003",
          "정복자 등급만 등록할 수 있는 쿠폰입니다.",
          HttpStatus.CONFLICT);
    }
  }

  // 쿠폰 유효기간 확인
  public void checkCouponExpiration(Coupon coupon) {
    LocalDate currentDate = LocalDate.now(clock);
    if (currentDate.isAfter(coupon.getValidUntil())) {
      throw new CustomException("COUPON#4_004",
          "해당 쿠폰의 유효기간이 지났습니다.",
          HttpStatus.CONFLICT);
    }
  }

  // 쿠폰 조회
  public Coupon getCouponById(Long couponId) {
    return couponRepository.findById(couponId).orElseThrow(
        () -> new CustomException("COUPON#2_001",
            "해당 쿠폰이 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));
  }

  // 유저 조회
  public User getUserById(Long userId) {
    return userRepository.getReferenceById(userId);
  }

  // 응답 생성
  public MyCouponSaveResponse buildResponse(MyCoupon myCoupon) {
    Coupon associatedCoupon = myCoupon.getCoupon();

    return MyCouponSaveResponse.builder()
        .id(associatedCoupon.getId())
        .name(associatedCoupon.getName())
        .description(associatedCoupon.getDescription())
        .code(myCoupon.getCode())
        .status(myCoupon.getStatus())
        .discountAmount(associatedCoupon.getDiscountAmount())
        .validUntil(associatedCoupon.getValidUntil())
        .createdAt(myCoupon.getCreatedAt())
        .build();
  }
}