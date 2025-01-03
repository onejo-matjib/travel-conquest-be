package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponSyncService {

  private final CouponRepository couponRepository;
  private final MyCouponRepository myCouponRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  // 만료 쿠폰 자동 삭제
  @Scheduled(cron = "0 00 00 * * *")
  @Transactional
  public void deleteExpiredCoupons() {
    LocalDate currentDate = LocalDate.now();

    // 1. DB에서 만료된 쿠폰 ID 조회
    List<Long> expiredCouponIds = couponRepository.findExpiredCouponIds(currentDate);

    if (!expiredCouponIds.isEmpty()) {
      // 2. Redis에서 관련 키 삭제
      deleteRedisKeys(expiredCouponIds);

      // 3. DB에서 만료된 쿠폰 삭제
      myCouponRepository.deleteByCouponIds(expiredCouponIds);
      couponRepository.deleteByIds(expiredCouponIds);

      log.info("DB와 Redis에서 유효기간 지난 쿠폰 {}개 삭제 완료.", expiredCouponIds.size());
    } else {
      log.info("유효기간 지난 쿠폰 없음.");
    }
  }

  // 쿠폰 DB 동기화
  @Scheduled(cron = "0 00 00 * * *")
  @Transactional
  public void syncRedisToCouponDatabase() {
    log.info("Redis 데이터를 DB와 동기화 시작...");

    // DB의 모든 쿠폰 조회
    couponRepository.findAll().forEach(coupon -> {
      String redisKey = COUPON_COUNT_KEY_PREFIX + coupon.getId();
      String redisValue = redisTemplate.opsForValue().get(redisKey);

      if (redisValue != null) {
        // Redis 값으로 DB 업데이트
        coupon.setCount(Integer.parseInt(redisValue));
        couponRepository.save(coupon);
      }
    });

    log.info("Redis 데이터를 DB와 동기화 완료.");
  }

  // Redis 키 삭제 병렬 처리
  @Async
  public void deleteRedisKeys(List<Long> couponIds) {
    couponIds.forEach(couponId -> {
      String redisKey = COUPON_COUNT_KEY_PREFIX + couponId;
      redisTemplate.delete(redisKey);
    });
  }

  private boolean isNumeric(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    try {
      Long.parseLong(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}