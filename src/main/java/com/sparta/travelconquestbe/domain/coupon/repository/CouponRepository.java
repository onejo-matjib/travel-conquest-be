package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryQueryDsl {

  @Query("SELECT c.id FROM Coupon c WHERE c.validUntil < :currentDate")
  List<Long> findExpiredCouponIds(LocalDate currentDate);

  @Modifying
  @Transactional
  @Query("DELETE FROM Coupon c WHERE c.id IN :couponIds")
  void deleteByIds(List<Long> couponIds);
}
