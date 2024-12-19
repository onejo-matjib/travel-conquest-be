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
class MyCouponServiceTest {

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
}