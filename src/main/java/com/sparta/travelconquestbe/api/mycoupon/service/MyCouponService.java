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

    // 유저 권한 확인
    if (userInfo.getType().equals(UserType.USER)) {
      throw new CustomException("COUPON#3_002",
          "등업된 사용자가 아닙니다.",
          HttpStatus.FORBIDDEN);
    }

    // 쿠폰 유효성 검사
    Coupon coupon = couponRepository.findById(couponId).orElseThrow(
        () -> new CustomException("COUPON#2_001",
            "해당 쿠폰이 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));

    if (myCouponRepository.existsByCouponIdAndUserId(coupon.getId(), userInfo.getId())) {
      throw new CustomException("COUPON#4_001",
          "중복된 쿠폰이 존재합니다.",
          HttpStatus.CONFLICT);
    }

    // 쿠폰 수량 확인
    if (coupon.getCount() <= 0) {
      throw new CustomException("COUPON#4_002",
          "해당 쿠폰이 소진되었습니다.",
          HttpStatus.CONFLICT);
    }

    // 프리미엄 쿠폰 저장 시 유저 등급 확인
    if (coupon.getType().equals(CouponType.PREMIUM)
        && !(userInfo.getTitle().equals(Title.CONQUEROR) || userInfo.getType()
        .equals(UserType.ADMIN))) {
      throw new CustomException("COUPON#4_003 ",
          "정복자 등급만 등록할 수 있는 쿠폰입니다.",
          HttpStatus.CONFLICT);
    }

    // 쿠폰 코드 발급
    String couponCode = UUID.randomUUID().toString();
    User savedUser = userRepository.findById(userInfo.getId()).orElseThrow(
        () -> new CustomException("COUPON#2_002",
            "해당 유저가 존재하지 않습니다",
            HttpStatus.NOT_FOUND));

    // 쿠폰 저장
    MyCoupon myCoupon =
        MyCoupon.builder()
            .code(couponCode)
            .status(UseStatus.AVAILABLE)
            .user(savedUser)
            .coupon(coupon)
            .build();
    coupon.saveCoupon();
    myCouponRepository.save(myCoupon);

    return MyCouponSaveResponse.builder()
        .id(myCoupon.getId())
        .name(myCoupon.getCoupon().getName())
        .description(myCoupon.getCoupon().getDescription())
        .code(myCoupon.getCode())
        .status(myCoupon.getStatus())
        .discountAmount(myCoupon.getCoupon().getDiscountAmount())
        .validUntil(myCoupon.getCoupon().getValidUntil())
        .createdAt(myCoupon.getCreatedAt())
        .build();
  }

  // 쿠폰 유효기간 확인
  public void checkCouponExpiration(Coupon coupon) {
    LocalDate currentDate = LocalDate.now(clock);
    if (currentDate.isAfter(coupon.getValidUntil())) {
      throw new CustomException("COUPON#4_004", "해당 쿠폰의 유효기간이 지났습니다.", HttpStatus.CONFLICT);
    }
  }
}
