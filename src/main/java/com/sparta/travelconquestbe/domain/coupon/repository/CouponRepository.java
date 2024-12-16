package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryQueryDsl {

  @Modifying
  @Query("DELETE FROM Coupon c WHERE c.validUntil < :currentDate")
  void deleteByExpiredCoupons(@Param("currentDate") LocalDate currentDate);

  // 비관적 락 설정
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Coupon c WHERE c.id = :couponId")
  Optional<Coupon> findByIdWithPessimisticLock(@Param("couponId") Long couponId);
}
