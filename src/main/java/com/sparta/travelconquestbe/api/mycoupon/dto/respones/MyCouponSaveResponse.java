package com.sparta.travelconquestbe.api.mycoupon.dto.respones;

import com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
