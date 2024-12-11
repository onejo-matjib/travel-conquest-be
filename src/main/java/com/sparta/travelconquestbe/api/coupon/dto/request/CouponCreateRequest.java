package com.sparta.travelconquestbe.api.coupon.dto.request;

import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CouponCreateRequest {

  @NotBlank(message = "쿠폰 이름은 필수입니다.")
  private String name;

  @NotBlank(message = "쿠폰 설명은 필수입니다.")
  private String description;

  @NotNull(message = "쿠폰 유형은 필수입니다.")
  private CouponType type;

  @NotBlank(message = "쿠폰 코드는 필수입니다.")
  private String code;

  @NotNull(message = "할인 금액은 필수입니다.")
  private int discountAmount;

  @NotNull(message = "유효 기간은 필수입니다.")
  @FutureOrPresent(message = "유효 기간은 현재 날짜 이상이어야 합니다.")
  private LocalDate validUntil;

  @NotNull(message = "쿠폰 수량은 필수입니다.")
  private int count;
}
