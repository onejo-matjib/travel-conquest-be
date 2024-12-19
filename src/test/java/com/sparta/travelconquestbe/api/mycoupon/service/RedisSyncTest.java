//package com.sparta.travelconquestbe.api.mycoupon.service;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
//import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
//import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
//import java.time.LocalDate;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@SpringBootTest
//class RedisSyncTest {
//
//  @Autowired
//  private CouponRepository couponRepository;
//
//  @Autowired
//  private RedisTemplate<String, String> redisTemplate;
//
//  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";
//
//  private Coupon testCoupon;
//
//  @BeforeEach
//  void setUp() {
//    // 테스트 환경 초기화
//    couponRepository.deleteAll();
//
//    // 테스트 쿠폰 생성
//    testCoupon = Coupon.builder()
//        .name("Redis Sync Test Coupon")
//        .description("Sync Test Description")
//        .type(CouponType.NORMAL)
//        .discountAmount(500)
//        .validUntil(LocalDate.now().plusDays(7))
//        .count(100) // 초기 수량
//        .build();
//    couponRepository.save(testCoupon);
//
//    // Redis에 초기 값 설정
//    String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
//    redisTemplate.opsForValue().set(redisKey, String.valueOf(testCoupon.getCount()));
//  }
//
//  @Test
//  void redis_데이터가_DB로_동기화_되는지_테스트() {
//    // Redis의 쿠폰 수량 변경
//    String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
//    redisTemplate.opsForValue().decrement(redisKey, 30); // Redis 수량 30 감소 (70으로 변경)
//
//    // Redis의 변경된 값 확인
//    String updatedRedisValue = redisTemplate.opsForValue().get(redisKey);
//    assertThat(updatedRedisValue).isEqualTo("70");
//
//    // 스케줄러나 동기화 메서드 수동 호출 (테스트 환경에서는 수동 호출 필요)
//    syncRedisToDatabase();
//
//    // DB의 값 확인
//    Coupon updatedCoupon = couponRepository.findById(testCoupon.getId())
//        .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
//
//    assertThat(updatedCoupon.getCount()).isEqualTo(70); // DB 값이 Redis와 동일한지 확인
//  }
//
//  // 수동으로 Redis 데이터를 DB와 동기화
//  private void syncRedisToDatabase() {
//    couponRepository.findAll().forEach(coupon -> {
//      String redisKey = COUPON_COUNT_KEY_PREFIX + coupon.getId();
//      String redisValue = redisTemplate.opsForValue().get(redisKey);
//
//      if (redisValue != null) {
//        coupon.setCount(Integer.parseInt(redisValue));
//        couponRepository.save(coupon);
//      }
//    });
//  }
//}
