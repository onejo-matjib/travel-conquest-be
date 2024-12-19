package com.sparta.travelconquestbe.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisToDbSyncSchedulerTest {

  @Autowired
  private CouponScheduler couponScheduler;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  @BeforeEach
  void setUp() {
    // 데이터 초기화
    couponRepository.deleteAll();

    // 테스트 쿠폰 생성
    Coupon testCoupon = Coupon.builder()
        .name("Redis Sync Coupon")
        .description("Sync Test Description")
        .type(CouponType.NORMAL)
        .discountAmount(500)
        .validUntil(LocalDate.now().plusDays(7))
        .count(100) // 초기 수량
        .build();
    couponRepository.save(testCoupon);

    // Redis에 초기 값 저장
    String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
    redisTemplate.opsForValue().set(redisKey, "70"); // Redis의 수량을 70으로 설정
  }

  @Test
  void Redis_DB_동기화_테스트() {
    // 스케줄러 실행
    couponScheduler.syncRedisToDatabase();

    // DB에서 쿠폰 수량 확인
    Coupon updatedCoupon = couponRepository.findAll().get(0);
    assertThat(updatedCoupon.getCount()).isEqualTo(70); // Redis 값이 DB에 반영되었는지 확인

    // Redis의 값도 동일한지 확인
    String redisKey = COUPON_COUNT_KEY_PREFIX + updatedCoupon.getId();
    String redisValue = redisTemplate.opsForValue().get(redisKey);
    assertThat(redisValue).isEqualTo("70");
  }
}
