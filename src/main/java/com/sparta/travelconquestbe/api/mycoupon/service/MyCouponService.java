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
import java.util.concurrent.TimeUnit;
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

  private static final long LOCK_TIMEOUT = 1000L; // 타임아웃
  private static final long RETRY_DELAY = 100L; // 재시도 간격

  @Transactional
  public MyCouponSaveResponse createMyCoupon(Long couponId, AuthUserInfo userInfo) {
    String lockKey = "couponId:" + couponId;
    String lockValue = String.valueOf(couponId);

    try {
      acquireLock(lockKey, lockValue); // Redis 락을 획득
      validateUser(userInfo); // 사용자 인증과 권한을 확인
      Coupon coupon = validateAndGetCoupon(couponId, userInfo); // 쿠폰 정보 검증

      String couponCode = UUID.randomUUID().toString(); // 쿠폰 고유번호 랜덤 생성
      User referenceUser = userRepository.getReferenceById(userInfo.getId()); // 프록시 객체 생성

      MyCoupon myCoupon = saveMyCoupon(coupon, referenceUser, couponCode);
      coupon.decrementCount(); // 쿠폰 수량 -1

      return buildResponse(myCoupon);
    } catch (Exception e) {
      throw new CustomException("COUPON#5_001", "쿠폰 발급 중 내부적인 문제가 발생했습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      releaseLock(lockKey);
    }
  }

  public void acquireLock(String lockKey, String lockValue) {
    try {
      while (!redisTemplate.opsForValue()
          .setIfAbsent(lockKey, lockValue, LOCK_TIMEOUT, TimeUnit.SECONDS)) {
        Thread.sleep(RETRY_DELAY);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException("COUPON#5_002", "잠금 처리 중 오류가 발생했습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void releaseLock(String lockKey) {
    redisTemplate.delete(lockKey);
  }

  public void validateUser(AuthUserInfo userInfo) {
    if (userInfo.getType().equals(UserType.USER)) {
      throw new CustomException("COUPON#3_002", "인증된 사용자가 아닙니다.", HttpStatus.FORBIDDEN);
    }
  }

  public Coupon validateAndGetCoupon(Long couponId, AuthUserInfo userInfo) {
    Coupon coupon = couponRepository.findById(couponId).orElseThrow(
        () -> new CustomException("COUPON#2_001",
            "해당 쿠폰이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (coupon.getType().equals(CouponType.PREMIUM) && !userInfo.getTitle()
        .equals(Title.CONQUEROR)) {
      throw new CustomException("COUPON#4_003",
          "정복자 등급만 등록할 수 있는 쿠폰입니다.", HttpStatus.CONFLICT);
    }

    if (myCouponRepository.existsByCouponIdAndUserIdAndStatus(coupon.getId(), userInfo.getId(),
        UseStatus.AVAILABLE)) {
      throw new CustomException("COUPON#4_001",
          "중복된 사용 가능한 쿠폰이 존재합니다.", HttpStatus.CONFLICT);
    }

    if (coupon.getCount() <= 0) {
      throw new CustomException("COUPON#4_002",
          "해당 쿠폰이 소진되었습니다.", HttpStatus.CONFLICT);
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
