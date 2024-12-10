package com.sparta.travelconquestbe.api.mycoupon.service;

import static com.sparta.travelconquestbe.domain.coupon.enums.CouponType.PRIMIUM;
import static com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus.AVAILABLE;
import static com.sparta.travelconquestbe.domain.user.enums.Title.CONQUEROR;
import static com.sparta.travelconquestbe.domain.user.enums.UserType.ADMIN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.sparta.travelconquestbe.api.mycoupon.dto.respones.MyCouponSaveResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyCouponService {

  private final MyCouponRepository myCouponRepository;
  private final UserRepository userRepository;
  private final CouponRepository couponRepository;
  private final Clock clock;

  @Transactional
  public MyCouponSaveResponse saveCoupon(Long couponId, Long userId) {
    User user = validateUser(userId);

    Coupon coupon = validateCoupon(couponId, user);

    MyCoupon myCoupon = SaveCoupon(user, coupon);

    return buildResponse(myCoupon);
  }

  // 유저 유효성 검사
  @Transactional(readOnly = true)
  public User validateUser(Long userId) {
    return userRepository.findById(userId)
        .filter(this::isAuthorizedUser)
        .orElseThrow(
            () -> new CustomException("COUPON_003",
                "해당 유저가 존재하지 않거나 권한이 없습니다.", FORBIDDEN));
  }

  // 쿠폰 유효성 검사
  @Transactional(readOnly = true)
  public Coupon validateCoupon(Long couponId, User user) {
    Coupon coupon = findCouponById(couponId);

    checkCouponDuplicate(coupon, user);
    checkCouponAvailability(coupon);
    checkPremiumCouponAccess(coupon, user);

    return coupon;
  }

  // 유저 권한 확인
  public boolean isAuthorizedUser(User user) {
    if (user.getType().equals(UserType.USER)) {
      throw new CustomException("COUPON_008", "등업된 사용자가 아닙니다.", FORBIDDEN);
    }
    return true;
  }

  // 쿠폰 DB 확인
  private Coupon findCouponById(Long couponId) {
    return couponRepository.findById(couponId)
        .orElseThrow(() -> new CustomException("COUPON_002",
            "해당 쿠폰이 존재하지 않습니다.", NOT_FOUND));
  }

  // 중복 여부 확인
  private void checkCouponDuplicate(Coupon coupon, User user) {
    if (myCouponRepository.existsByCouponIdAndUserId(coupon.getId(), user.getId())) {
      throw new CustomException("COUPON_010", "중복된 쿠폰이 존재합니다.", CONFLICT);
    }
  }

  // 쿠폰 수량 확인
  private void checkCouponAvailability(Coupon coupon) {
    if (coupon.getCount() <= 0) {
      throw new CustomException("COUPON_005", "해당 쿠폰이 소진되었습니다.", CONFLICT);
    }
  }

  // 프리미엄 쿠폰 저장 시 유저 등급 확인
  private void checkPremiumCouponAccess(Coupon coupon, User user) {
    if (coupon.getType().equals(PRIMIUM)
        && !(user.getTitle().equals(CONQUEROR) || user.getType().equals(ADMIN))) {
      throw new CustomException("COUPON_009", "정복자 등급만 등록할 수 있는 쿠폰입니다.", CONFLICT);
    }
  }

  // 쿠폰 유효기간 확인
  private void checkCouponExpiration(Coupon coupon) {
    LocalDate currentDate = LocalDate.now(clock);
    if (currentDate.isAfter(coupon.getValidUntil())) {
      throw new CustomException("COUPON_006", "해당 쿠폰의 유효기간이 지났습니다.", BAD_REQUEST);
    }
  }

  // 쿠폰 저장
  private MyCoupon SaveCoupon(User user, Coupon coupon) {
    MyCoupon myCoupon = MyCoupon.builder()
        .status(AVAILABLE)
        .user(user)
        .coupon(coupon)
        .build();

    coupon.saveCoupon();
    myCouponRepository.save(myCoupon);

    return myCoupon;
  }

  // 응답 생성
  private MyCouponSaveResponse buildResponse(MyCoupon myCoupon) {
    return MyCouponSaveResponse.builder()
        .id(myCoupon.getId())
        .name(myCoupon.getCoupon().getName())
        .description(myCoupon.getCoupon().getDescription())
        .code(myCoupon.getCoupon().getCode())
        .status(myCoupon.getStatus())
        .discountAmount(myCoupon.getCoupon().getDiscountAmount())
        .validUntil(myCoupon.getCoupon().getValidUntil())
        .createdAt(myCoupon.getCreatedAt())
        .build();
  }
}
