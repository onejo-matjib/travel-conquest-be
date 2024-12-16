//package com.sparta.travelconquestbe.api.mycoupon.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
//import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
//import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
//import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
//import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
//import com.sparta.travelconquestbe.domain.user.entity.User;
//import com.sparta.travelconquestbe.domain.user.enums.Title;
//import com.sparta.travelconquestbe.domain.user.enums.UserType;
//import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class MyCouponServiceConcurrencyTest {
//
//  @Autowired
//  private MyCouponService myCouponService;
//
//  @Autowired
//  private CouponRepository couponRepository;
//
//  @Autowired
//  private UserRepository userRepository;
//
//  private Coupon testCoupon;
//  private List<User> testUsers;
//
//  @Autowired
//  private MyCouponRepository myCouponRepository;
//
//  @BeforeEach
//  void setUp() {
//    // 기존 데이터 초기화
//    myCouponRepository.deleteAll();
//    couponRepository.deleteAll();
//    userRepository.deleteAll();
//
//    // 테스트 쿠폰 생성
//    testCoupon = Coupon.builder()
//        .name("Test Coupon")
//        .description("Test Description")
//        .type(CouponType.NORMAL)
//        .discountAmount(1000)
//        .validUntil(LocalDate.now().plusDays(7))
//        .count(100)
//        .build();
//    couponRepository.save(testCoupon);
//
//    // 테스트 사용자 생성
//    testUsers = IntStream.rangeClosed(1, 10000)
//        .mapToObj(i -> User.builder()
//            .name("User" + i)
//            .nickname("Nickname" + i)
//            .email("user" + i + "@test.com")
//            .password("password")
//            .birth("2000-01-01")
//            .type(UserType.AUTHENTICATED_USER)
//            .title(Title.CONQUEROR)
//            .build())
//        .collect(Collectors.toList());
//    userRepository.saveAll(testUsers);
//  }
//
//  @Test
//  void 동시성_쿠폰_발급_테스트() throws InterruptedException {
//    // 동시 쓰레드 수 (실제 쿠폰 수보다 많게 설정)
//    int threadCount = 10000;
//
//    // 쓰레드 풀 생성
//    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//
//    // 동시성 제어를 위한 래치
//    CountDownLatch latch = new CountDownLatch(threadCount);
//
//    // 동시 쿠폰 발급 시도
//    for (int i = 0; i < threadCount; i++) {
//      User currentUser = testUsers.get(i % testUsers.size());
//
//      // 사용자 인증 정보 생성
//      AuthUserInfo userInfo = new AuthUserInfo(
//          currentUser.getId(),
//          currentUser.getName(),
//          currentUser.getNickname(),
//          currentUser.getEmail(),
//          currentUser.getPassword(),
//          currentUser.getBirth(),
//          currentUser.getType(),
//          currentUser.getTitle()
//      );
//
//      executorService.submit(() -> {
//        try {
//          // 쿠폰 발급 시도
//          myCouponService.createMyCoupon(testCoupon.getId(), userInfo);
//          System.out.println("쿠폰 발급 성공");
//        } catch (Exception e) {
//          // 예외 발생 시 로깅 (쿠폰 소진 등의 예외는 정상 처리)
//          System.out.println("쿠폰 발급 중 예외 발생: " + e.getMessage());
//        } finally {
//          // 작업 완료 래치 카운트 감소
//          latch.countDown();
//        }
//      });
//    }
//
//    // 모든 쓰레드 작업 완료 대기
//    latch.await();
//
//    // 쓰레드 풀 종료
//    executorService.shutdown();
//
//    // 최종 쿠폰 상태 조회
//    Coupon updatedCoupon = couponRepository.findById(testCoupon.getId())
//        .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
//
//    // assertions
//    System.out.println("최종 쿠폰 잔여수량: " + updatedCoupon.getCount());
//    assertThat(updatedCoupon.getCount()).isEqualTo(0); // 모두 소진되어야 가능
//  }
//}
//
