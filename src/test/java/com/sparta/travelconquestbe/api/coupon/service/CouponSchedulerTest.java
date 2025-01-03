//package com.sparta.travelconquestbe.api.coupon.service;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
//import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
//import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
//import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
//import java.time.LocalDate;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@SpringBootTest
//class CouponSynTest {
//
//  @Autowired
//  private CouponSyncService couponSyncService;
//
//  @Autowired
//  private CouponRepository couponRepository;
//
//  @Autowired
//  private MyCouponRepository myCouponRepository;
//
//  @Autowired
//  private RedisTemplate<String, String> redisTemplate;
//
//  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";
//
//  @BeforeEach
//  void setUp() {
//    // 데이터 초기화
//    myCouponRepository.deleteAll();
//    couponRepository.deleteAll();
//
//    // 만료된 쿠폰 생성
//    Coupon expiredCoupon = Coupon.builder()
//        .name("Expired Coupon")
//        .description("This is expired")
//        .type(CouponType.NORMAL)
//        .discountAmount(500)
//        .validUntil(LocalDate.now().minusDays(1)) // 만료된 날짜
//        .count(50)
//        .build();
//    couponRepository.save(expiredCoupon);
//
//    // 유효한 쿠폰 생성
//    Coupon validCoupon = Coupon.builder()
//        .name("Valid Coupon")
//        .description("This is valid")
//        .type(CouponType.NORMAL)
//        .discountAmount(500)
//        .validUntil(LocalDate.now().plusDays(7)) // 유효한 날짜
//        .count(50)
//        .build();
//    couponRepository.save(validCoupon);
//
//    // Redis에 만료된 쿠폰과 유효한 쿠폰 등록
//    redisTemplate.opsForValue().set(COUPON_COUNT_KEY_PREFIX + expiredCoupon.getId(), "50");
//    redisTemplate.opsForValue().set(COUPON_COUNT_KEY_PREFIX + validCoupon.getId(), "50");
//  }
//
//  @Test
//  void 만료_쿠폰_삭제_테스트() {
//    // 스케줄러 실행
//    couponSyncService.deleteExpiredCoupons();
//
//    // 만료된 쿠폰이 삭제되었는지 확인
//    List<Long> expiredCouponIds = couponRepository.findExpiredCouponIds(LocalDate.now());
//    System.out.println("만료된 쿠폰 ID: " + expiredCouponIds);
//
//    List<Coupon> remainingCoupons = couponRepository.findAll();
//    System.out.println("남아 있는 쿠폰: " + remainingCoupons);
//    assertThat(remainingCoupons).hasSize(1); // 유효한 쿠폰만 남아 있어야 함
//
//    Coupon remainingCoupon = remainingCoupons.get(0);
//    assertThat(remainingCoupon.getName()).isEqualTo("Valid Coupon");
//
//    // Redis에서도 만료된 쿠폰 키가 삭제되었는지 확인
//    String expiredRedisKey = COUPON_COUNT_KEY_PREFIX + "1"; // 예상된 만료된 쿠폰 ID
//    String expiredRedisValue = redisTemplate.opsForValue().get(expiredRedisKey);
//    assertThat(expiredRedisValue).isNull(); // Redis 키 삭제 확인
//
//    // 유효한 쿠폰의 Redis 값 확인
//    String validRedisKey = COUPON_COUNT_KEY_PREFIX + remainingCoupon.getId();
//    String validRedisValue = redisTemplate.opsForValue().get(validRedisKey);
//    assertThat(validRedisValue).isEqualTo("50"); // 유효한 쿠폰의 Redis 데이터 유지
//  }
//}
