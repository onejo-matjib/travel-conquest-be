package com.sparta.travelconquestbe.api.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponSort;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        .discountAmount(10000)
        .validUntil(LocalDate.of(2024, 12, 24))
        .build();

    Coupon coupon2 = Coupon.builder()
        .id(2L)
        .name("테스트2")
        .description("테스트 설명2")
        .discountAmount(20000)
        .validUntil(LocalDate.of(2024, 12, 25))
        .build();

    List<CouponSearchResponse> responses = List.of(
        new CouponSearchResponse(
            coupon1.getId(),
            coupon1.getName(),
            coupon1.getDescription(),
            coupon1.getType(),
            coupon1.getDiscountAmount(),
            coupon1.getValidUntil(),
            coupon1.getCount(),
            coupon1.getCreatedAt(),
            coupon1.getUpdatedAt()
        ),
        new CouponSearchResponse(
            coupon2.getId(),
            coupon2.getName(),
            coupon2.getDescription(),
            coupon2.getType(),
            coupon2.getDiscountAmount(),
            coupon2.getValidUntil(),
            coupon2.getCount(),
            coupon2.getCreatedAt(),
            coupon2.getUpdatedAt()
        )
    );

    Page<CouponSearchResponse> mockPage = new PageImpl<>(
        responses,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "VALID_UNTIL")),
        responses.size()
    );

    // When: Repository 동작을 Mocking
    Pageable pageable = PageRequest.of(0, 10);
    when(couponRepository.searchAllCoupons(pageable, CouponSort.VALID_UNTIL, "DESC"))
        .thenReturn(mockPage);

    // When: 서비스 메서드 호출
    Page<CouponSearchResponse> result = couponService.searchAllCoupons(
        pageable, CouponSort.VALID_UNTIL, "DESC");
    // Then: 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().get(0).getName()).isEqualTo("테스트1");
    assertThat(result.getContent().get(1).getName()).isEqualTo("테스트2");
    assertThat(result.getContent().get(0).getDiscountAmount()).isEqualTo(10000);
    assertThat(result.getContent().get(1).getDiscountAmount()).isEqualTo(20000);
  }
}