package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchAllCoupons() {

        // Given: Mock 데이터를 준비
        Coupon coupon1 = Coupon.builder()
                .id(1L)
                .name("테스트1")
                .description("테스트 설명1")
                .code("testCode1")
                .discountAmount(10000)
                .validUntil(LocalDate.of(2024, 12, 24))
                .build();

        Coupon coupon2 = Coupon.builder()
                .id(2L)
                .name("테스트2")
                .description("테스트 설명2")
                .code("testCode2")
                .discountAmount(10000)
                .validUntil(LocalDate.of(2024, 12, 24))
                .build();

        List<CouponSearchResponse> responses = List.of(
                new CouponSearchResponse(
                        coupon1.getId(),
                        coupon1.getName(),
                        coupon1.getDescription(),
                        coupon1.getCode(),
                        coupon1.getDiscountAmount(),
                        coupon1.getValidUntil(),
                        coupon1.getCount(),
                        coupon1.getCreatedAt(),
                        coupon1.getUpdatedAt()
                )
                ,
                new CouponSearchResponse(
                        coupon2.getId(),
                        coupon2.getName()
                        , coupon2.getDescription(),
                        coupon2.getCode(),
                        coupon2.getDiscountAmount(),
                        coupon2.getValidUntil(),
                        coupon1.getCount(),
                        coupon1.getCreatedAt(),
                        coupon1.getUpdatedAt()
                )
        );

        Page<CouponSearchResponse> mockPage = new PageImpl<>(
                responses,
                PageRequest.of(0, 10), responses.size());

        // When: Repository 동작을 Mocking
        when(couponRepository.searchAllCoupons(
                PageRequest.of(0, 10))).thenReturn(mockPage);

        // When: 서비스 메서드 호출
        Page<CouponSearchResponse> result = couponService.searchAllCoupons(1, 10);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트1");
        assertThat(result.getContent().get(1).getName()).isEqualTo("테스트2");
    }
}
