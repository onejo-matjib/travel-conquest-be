package com.sparta.travelconquestbe.api.mycoupon.dto.response;

import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyCouponListResponse {

  private Long id;
  private String name;
  private String description;
  private CouponType type;
  private String code;
  private UseStatus status;
  private int discountAmount;
  private LocalDate validUntil;
  private LocalDateTime createdAt;

  public MyCouponListResponse(Long id, String name, String description, CouponType type,
      String code,
      UseStatus status, int discountAmount, LocalDate validUntil, LocalDateTime createdAt) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.type = type;
    this.code = code;
    this.status = status;
    this.discountAmount = discountAmount;
    this.validUntil = validUntil;
    this.createdAt = createdAt;
  }
}
