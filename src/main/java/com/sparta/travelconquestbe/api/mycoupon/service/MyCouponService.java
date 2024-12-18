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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyCouponService {

  private final MyCouponRepository myCouponRepository;
  private final CouponRepository couponRepository;
  private final UserRepository userRepository;
  private final RedisTemplate<String, String> redisTemplate;

  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  @Transactional
  public MyCouponSaveResponse createMyCoupon(Long couponId, AuthUserInfo userInfo) {
    validateUser(userInfo);
    Coupon coupon = validateAndGetCoupon(couponId, userInfo);

    String redisKey = COUPON_COUNT_KEY_PREFIX + couponId;
    int redisCount = getOrInitializeCouponCount(redisKey, coupon);

    if (redisCount <= 0) {
      throw new CustomException("COUPON#4_002", "해당 쿠폰이 소진되었습니다.", HttpStatus.CONFLICT);
    }

    // Redis에서 수량 감소
    boolean updated = decreaseCouponCount(redisKey);
    if (!updated) {
      throw new CustomException("COUPON#4_002", "쿠폰 발급 중 수량 감소 실패.", HttpStatus.CONFLICT);
    }

    // 쿠폰 생성
    String couponCode = UUID.randomUUID().toString();
    User referenceUser = userRepository.getReferenceById(userInfo.getId());
    MyCoupon myCoupon = saveMyCoupon(coupon, referenceUser, couponCode);

    return buildResponse(myCoupon);
  }

  private int getOrInitializeCouponCount(String redisKey, Coupon coupon) {
    String cachedCount = redisTemplate.opsForValue().get(redisKey);
    if (cachedCount == null) {
      // Redis에 수량 초기화
      redisTemplate.opsForValue().set(redisKey, String.valueOf(coupon.getCount()));
      return coupon.getCount();
    }
    return Integer.parseInt(cachedCount);
  }

  private boolean decreaseCouponCount(String redisKey) {
    Long result = redisTemplate.opsForValue().decrement(redisKey);
    return result != null && result >= 0;
  }

  public void validateUser(AuthUserInfo userInfo) {
    if (userInfo.getType().equals(UserType.USER)) {
      throw new CustomException("COUPON#3_002", "인증된 사용자가 아닙니다.", HttpStatus.FORBIDDEN);
    }
  }

  public Coupon validateAndGetCoupon(Long couponId, AuthUserInfo userInfo) {
    Coupon coupon = couponRepository.findById(couponId).orElseThrow(
        () -> new CustomException("COUPON#2_001", "해당 쿠폰이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (coupon.getType().equals(CouponType.PREMIUM) && !userInfo.getTitle()
        .equals(Title.CONQUEROR)) {
      throw new CustomException("COUPON#4_003",
          "정복자 등급(CONQUEROR)만 등록할 수 있는 쿠폰입니다.", HttpStatus.CONFLICT);
    }

    if (myCouponRepository.existsByCouponIdAndUserIdAndStatus(coupon.getId(), userInfo.getId(),
        UseStatus.AVAILABLE)) {
      throw new CustomException("COUPON#4_001", "중복된 사용 가능한 쿠폰이 존재합니다.", HttpStatus.CONFLICT);
    }

    return coupon;
  }

  public MyCoupon saveMyCoupon(Coupon coupon, User user, String couponCode) {
    MyCoupon myCoupon = MyCoupon.builder()
        .code(couponCode)
        .status(UseStatus.AVAILABLE)
        .user(user)
        .coupon(coupon)
        .build();
    return myCouponRepository.save(myCoupon);
  }

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