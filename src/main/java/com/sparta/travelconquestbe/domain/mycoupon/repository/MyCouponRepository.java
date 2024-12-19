package com.sparta.travelconquestbe.domain.mycoupon.repository;

import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MyCouponRepository extends JpaRepository<MyCoupon, Long> {

  boolean existsByCouponIdAndUserIdAndStatus(Long couponId, Long userId, UseStatus status);

  @Modifying
  @Transactional
  @Query("DELETE FROM MyCoupon mc WHERE mc.coupon.id IN :couponIds")
  void deleteByCouponIds(List<Long> couponIds);
}
