package com.sparta.travelconquestbe.api.coupon.dto.respones;

import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CouponCreateResponse {

  private Long id;
  private String name;
  private String description;
  private CouponType type;
  private int discountAmount;
  private LocalDate validUntil;
  private int count;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
