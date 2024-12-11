package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryQueryDsl {

  @Modifying
  @Query("DELETE FROM Coupon c WHERE c.validUntil < :currentDate")
  void deleteByExpiredCoupons(@Param("currentDate") LocalDate currentDate);

}
