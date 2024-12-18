package com.sparta.travelconquestbe.domain.coupon.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends TimeStampCreateUpdate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CouponType type;

  @Column(nullable = false)
  private int discountAmount;

  @Column(nullable = false)
  private LocalDate validUntil;

  @Column(nullable = false)
  private int count;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "coupon", cascade = CascadeType.REMOVE, orphanRemoval = true)
  @BatchSize(size = 100)
  private final List<MyCoupon> myCoupons = new ArrayList<>();
}
