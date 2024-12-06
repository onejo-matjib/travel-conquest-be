package com.sparta.travelconquestbe.api.coupon.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponSearchResponse {
    private Long id;
    private String name;
    private String description;
    private String code;
    private int discountAmount;
    private LocalDate validUntil;
    private int count;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
