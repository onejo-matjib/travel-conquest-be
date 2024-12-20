package com.sparta.travelconquestbe.api.mycoupon.service;

import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponListResponse;
import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponSaveResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  public Page<MyCouponListResponse> searchAllMyCoupon(AuthUserInfo userInfo, int page, int limit,
      String sort, String direction) {
    Pageable pageable = PageRequest.of(page - 1, limit,
        direction.equalsIgnoreCase("DESC") ? Sort.by(sort).descending()
            : Sort.by(sort).ascending());

    return myCouponRepository.searchAllMyCoupon(userInfo.getId(), pageable);
  }

  @Transactional
  public MyCouponSaveResponse createMyCoupon(Long couponId, AuthUserInfo userInfo) {
    String lockKey = "couponId:" + couponId;
    String lockValue = String.valueOf(couponId);

    try {
      acquireLock(lockKey, lockValue); // Redis 락 획득

      validateUser(userInfo); // 사용자 검증
      Coupon coupon = validateAndGetCoupon(couponId, userInfo); // 쿠폰 검증

      String redisKey = COUPON_COUNT_KEY_PREFIX + couponId;
      String cachedCount = redisTemplate.opsForValue().get(redisKey);

      if (cachedCount == null) {
        redisTemplate.opsForValue().set(redisKey, String.valueOf(coupon.getCount()));
        cachedCount = String.valueOf(coupon.getCount());
      }

      int redisCount = Integer.parseInt(cachedCount);
      if (redisCount <= 0) {
        throw new CustomException("COUPON#4_002", "해당 쿠폰이 소진되었습니다.", HttpStatus.CONFLICT);
      }

      redisTemplate.opsForValue().set(redisKey, String.valueOf(redisCount - 1));
      String couponCode = UUID.randomUUID().toString();
      User referenceUser = userRepository.getReferenceById(userInfo.getId());
      MyCoupon myCoupon = saveMyCoupon(coupon, referenceUser, couponCode);

      if (redisCount - 1 == 0) {
        syncCouponCountToDatabase(coupon, 0);
      }

      return buildResponse(myCoupon);

    } catch (CustomException e) {
      throw e;  // 이미 처리된 커스텀 예외는 다시 던지기
    } catch (NumberFormatException e) {
      throw new CustomException("COUPON#5_002", "쿠폰 수량 변환 중 오류 발생.", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      throw new CustomException("COUPON#5_001", "쿠폰 발급 중 내부적인 문제가 발생했습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      releaseLock(lockKey);
    }
  }

  // Redis에서 발급된 쿠폰 수량을 DB로 동기화
  public void syncCouponCountToDatabase(Coupon coupon, int newRedisCount) {
    coupon.setCount(newRedisCount); // DB의 쿠폰 수량 업데이트
    couponRepository.save(coupon); // DB에 저장
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
          "정복자 등급(CONQUEROR)만 등록할 수 있는 쿠폰입니다.", HttpStatus.CONFLICT);
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