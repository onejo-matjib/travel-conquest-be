package com.sparta.travelconquestbe.api.coupon.dto.respones;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponSearchResponse {

  private Long id;
  private String name;
  private String description;
  private int discountAmount;
  private LocalDate validUntil;
  private int count;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
