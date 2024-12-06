package com.sparta.travelconquestbe.domain.mycoupon.repository;

import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyCouponRepository extends JpaRepository<MyCoupon, Long> {
}
