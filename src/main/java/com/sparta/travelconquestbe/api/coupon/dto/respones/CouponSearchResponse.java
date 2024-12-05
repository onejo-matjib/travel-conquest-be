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
    private int discountAmount;
    private LocalDate validUntil;
    private int count;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CouponSearchResponse(Long id, String name, String description, String code, int discountAmount, LocalDate validUntil, int count, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.code = code;
        this.discountAmount = discountAmount;
        this.validUntil = validUntil;
        this.count = count;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
