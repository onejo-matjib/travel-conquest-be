package com.sparta.travelconquestbe.api.mycoupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class MyCouponServiceConcurrencyTest {

  @Autowired
  private MyCouponService myCouponService;

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MyCouponRepository myCouponRepository;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private Coupon testCoupon;
  private List<User> testUsers;

  private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

  @BeforeEach
  void setUp() {
    myCouponRepository.deleteAll();
    couponRepository.deleteAll();
    userRepository.deleteAll();

    testCoupon = Coupon.builder()
        .name("Test Coupon")
        .description("Test Description")
        .type(CouponType.NORMAL)
        .discountAmount(1000)
        .validUntil(LocalDate.now().plusDays(7))
        .count(100)
        .build();
    couponRepository.save(testCoupon);

    testUsers = IntStream.rangeClosed(1, 10000)
        .mapToObj(i -> User.builder()
            .name("User" + i)
            .nickname("Nickname" + i)
            .email("user" + i + "@test.com")
            .password("password")
            .birth("2000-01-01")
            .type(UserType.AUTHENTICATED_USER)
            .title(Title.CONQUEROR)
            .subscriptionCount(1)
            .build())
        .collect(Collectors.toList());
    userRepository.saveAll(testUsers);
  }

  @Test
  void 동시성_쿠폰_발급_및_redisCount_검증() throws InterruptedException {
    int threadCount = 10000;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      User currentUser = testUsers.get(i % testUsers.size());
      AuthUserInfo userInfo = new AuthUserInfo(
          currentUser.getId(),
          currentUser.getName(),
          currentUser.getNickname(),
          currentUser.getEmail(),
          currentUser.getPassword(),
          currentUser.getBirth(),
          currentUser.getType(),
          currentUser.getTitle()
      );

      executorService.submit(() -> {
        try {
          myCouponService.createMyCoupon(testCoupon.getId(), userInfo);
        } catch (Exception e) {
          System.out.println("쿠폰 발급 중 예외 발생: " + e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
    String redisCount = redisTemplate.opsForValue().get(redisKey);

    Coupon updatedCoupon = couponRepository.findById(testCoupon.getId())
        .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));

    System.out.println("Redis 쿠폰 잔여수량: " + redisCount);
    System.out.println("DB 쿠폰 잔여수량: " + updatedCoupon.getCount());

    assertThat(Integer.parseInt(redisCount)).isEqualTo(updatedCoupon.getCount());
    assertThat(updatedCoupon.getCount()).isEqualTo(0);
  }

  @SpringBootTest
  class RedisSyncTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
      // 테스트 환경 초기화
      couponRepository.deleteAll();

      // 테스트 쿠폰 생성
      testCoupon = Coupon.builder()
          .name("Redis Sync Test Coupon")
          .description("Sync Test Description")
          .type(CouponType.NORMAL)
          .discountAmount(500)
          .validUntil(LocalDate.now().plusDays(7))
          .count(100) // 초기 수량
          .build();
      couponRepository.save(testCoupon);

      // Redis에 초기 값 설정
      String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
      redisTemplate.opsForValue().set(redisKey, String.valueOf(testCoupon.getCount()));
    }

    @Test
    void redis_데이터가_DB로_동기화_되는지_테스트() {
      // Redis의 쿠폰 수량 변경
      String redisKey = COUPON_COUNT_KEY_PREFIX + testCoupon.getId();
      redisTemplate.opsForValue().decrement(redisKey, 30); // Redis 수량 30 감소 (70으로 변경)

      // Redis의 변경된 값 확인
      String updatedRedisValue = redisTemplate.opsForValue().get(redisKey);
      assertThat(updatedRedisValue).isEqualTo("70");

      // 스케줄러나 동기화 메서드 수동 호출 (테스트 환경에서는 수동 호출 필요)
      syncRedisToDatabase();

      // DB의 값 확인
      Coupon updatedCoupon = couponRepository.findById(testCoupon.getId())
          .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));

      assertThat(updatedCoupon.getCount()).isEqualTo(70); // DB 값이 Redis와 동일한지 확인
    }

    // 수동으로 Redis 데이터를 DB와 동기화
    private void syncRedisToDatabase() {
      couponRepository.findAll().forEach(coupon -> {
        String redisKey = COUPON_COUNT_KEY_PREFIX + coupon.getId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue != null) {
          coupon.setCount(Integer.parseInt(redisValue));
          couponRepository.save(coupon);
        }
      });
    }
  }

}