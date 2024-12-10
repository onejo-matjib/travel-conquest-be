package com.sparta.travelconquestbe.domain.mycoupon.repository;

import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MyCouponRepository extends JpaRepository<MyCoupon, Long> {

  boolean existsByCouponIdAndUserId(Long couponId, Long userId);

  @Modifying
  @Query("DELETE FROM MyCoupon mc WHERE mc.coupon.validUntil < :currentDate")
  void deleteByExpiredCoupons(@Param("currentDate") LocalDate currentDate);
}
