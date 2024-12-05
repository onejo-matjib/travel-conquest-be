package com.sparta.travelconquestbe.api.coupon.dto.respones;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CouponSearchResponse {
    private Long id;
    private String name;
    private String description;
    private String code;
    private int discount_amount;
    private LocalDate valid_until;
    private int count;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
