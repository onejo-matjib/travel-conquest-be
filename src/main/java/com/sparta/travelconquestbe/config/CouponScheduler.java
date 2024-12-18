package com.sparta.travelconquestbe.config;

import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
public class CouponScheduler {

  private final CouponRepository couponRepository;
  private final MyCouponRepository myCouponRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  // 만료 쿠폰 자동 삭제
  @Scheduled(cron = "0 00 00 * * *")
  @Transactional
  public void deleteExpiredCoupons() {
    LocalDate currentDate = LocalDate.now();
    // 유효기간이 지난 쿠폰 ID 조회
    List<Long> expiredCouponIds = getExpiredCouponIdsFromCache(currentDate);

    if (!expiredCouponIds.isEmpty()) {
      myCouponRepository.deleteByCouponIds(expiredCouponIds);
      deleteRedisKeys(expiredCouponIds); // 비동기 호출
      couponRepository.deleteByIds(expiredCouponIds);
      log.info("유효기간 지난 쿠폰 {}개 삭제 완료.", expiredCouponIds.size());
    } else {
      log.info("유효기간 지난 쿠폰 없음.");
    }
  }

  // 쿠폰 DB 동기화
  @Scheduled(cron = "0 00 00 * * *")
  @Transactional
  public void syncRedisToDatabase() {
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

  // 쿠폰 ID를 Redis에 캐싱
  public List<Long> getExpiredCouponIdsFromCache(LocalDate currentDate) {
    String cacheKey = "expired_coupons:" + currentDate.toString();

    // Redis에서 데이터 가져오기
    List<String> cachedCouponIds = redisTemplate.opsForList().range(cacheKey, 0, -1);
    List<Long> couponIds;

    if (cachedCouponIds == null || cachedCouponIds.isEmpty()) {
      // Redis에 데이터가 없으면 DB 조회
      couponIds = couponRepository.findExpiredCouponIds(currentDate);

      // Redis에 데이터 캐싱
      redisTemplate.opsForList().rightPushAll(cacheKey, couponIds.stream()
          .map(String::valueOf)
          .collect(Collectors.toList()));
      redisTemplate.expire(cacheKey, Duration.ofHours(24)); // 24시간 캐싱
    } else {
      // Redis 데이터를 안전하게 Long 리스트로 변환
      couponIds = cachedCouponIds.stream()
          .filter(this::isNumeric) // 숫자 문자열만 처리
          .map(Long::parseLong)
          .collect(Collectors.toList());
    }
    return couponIds;
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