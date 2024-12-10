package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
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
  private final MyCouponRepository myCouponRepository;

  // 매일 자정 실행
  @Scheduled(cron = "0 55 20 * * *")
  @Transactional
  public void deleteExpiredCoupons() {
    LocalDate currentDate = LocalDate.now();

    // 유효기간 지난 MyCoupon 삭제
    myCouponRepository.deleteExpiredMyCoupons(currentDate);

    // 유효기간 지난 Coupon 삭제
    couponRepository.deleteByExpiredCoupons(currentDate);
  }
}





