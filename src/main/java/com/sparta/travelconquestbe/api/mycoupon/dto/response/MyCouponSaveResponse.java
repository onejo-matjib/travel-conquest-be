package com.sparta.travelconquestbe.api.mycoupon.dto.response;

import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyCouponSaveResponse {

  private Long id;
  private String name;
  private String description;
  private String code;
  private UseStatus status;
  private int discountAmount;
  private LocalDate validUntil;
  private LocalDateTime createdAt;
}