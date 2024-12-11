package com.sparta.travelconquestbe.domain.coupon.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupons")
@Getter
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

  @OneToMany(mappedBy = "coupon")
  @Column(nullable = false)
  private List<MyCoupon> myCoupons;

  public void saveCoupon() {
    this.count -= 1;
  }
}
