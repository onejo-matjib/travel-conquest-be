package com.sparta.travelconquestbe.api.coupon.dto.respones;

import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponSaveResponse {
    private Long id;
    private String name;
    private String description;
    private String code;
    private UseStatus status;
    private int discountAmount;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
}
