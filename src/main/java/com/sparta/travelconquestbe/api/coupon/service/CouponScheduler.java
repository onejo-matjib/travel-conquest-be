package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

  private final CouponRepository couponRepository;

  // 매일 자정 실행
  @Scheduled(cron = "0 00 00 * * *")
  @Transactional
  public void deleteExpiredCoupons() {
    LocalDate currentDate = LocalDate.now();

    // 유효기간 지난 Coupon 삭제(MyCoupon 영속성 전이)
    couponRepository.deleteByValidUntilBefore(currentDate);
  }
}





