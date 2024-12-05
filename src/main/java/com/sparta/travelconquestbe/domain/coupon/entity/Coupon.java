package com.sparta.travelconquestbe.domain.coupon.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends TimeStampCreateUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private int discount_amount;

    @Column(nullable = false)
    private LocalDate valid_until;

    @Column(nullable = false)
    private int count;
}
