package com.sparta.travelconquestbe.domain.coupon.repository;

import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {
}
