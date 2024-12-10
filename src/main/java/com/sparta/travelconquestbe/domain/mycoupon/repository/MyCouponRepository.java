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
  @Query("DELETE FROM MyCoupon mc WHERE mc.coupon.id IN " +
      "(SELECT c.id FROM Coupon c WHERE c.validUntil < :currentDate)")
  void deleteExpiredMyCoupons(@Param("currentDate") LocalDate currentDate);

}
