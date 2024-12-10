package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryQueryDsl {

  void deleteByValidUntilBefore(LocalDate currentDate);
}
