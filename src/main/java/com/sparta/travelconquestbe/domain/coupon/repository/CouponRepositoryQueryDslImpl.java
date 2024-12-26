package com.sparta.travelconquestbe.domain.coupon.repository;

import static com.sparta.travelconquestbe.domain.coupon.entity.QCoupon.coupon;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponSort;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryQueryDslImpl implements CouponRepositoryQueryDsl {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<CouponSearchResponse> searchAllCoupons(
      Pageable pageable,
      CouponSort couponSort,
      String direction
  ) {
    QueryResults<CouponSearchResponse> results = jpaQueryFactory
        .select(Projections.constructor(CouponSearchResponse.class,
            coupon.id,
            coupon.name,
            coupon.description,
            coupon.discountAmount,
            coupon.validUntil,
            coupon.count,
            coupon.createdAt,
            coupon.updatedAt
        ))
        .from(coupon)
        .groupBy(coupon.id)
        .orderBy(getOrderSpecifiers(couponSort, direction).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<CouponSearchResponse> content = results.getResults();
    long totalCount = results.getTotal();

    return new PageImpl<>(content, pageable, totalCount);
  }

  private List<OrderSpecifier<?>> getOrderSpecifiers(CouponSort couponSort, String direction) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    // 정렬 방향 설정 (기본값: DESC)
    Order sortOrder = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

    // 특정 필드에 대한 매핑
    switch (couponSort) {
      case NAME -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.name));
      case TYPE -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.type));
      case COUNT -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.count));
      case DISCOUNT_AMOUNT ->
          orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.discountAmount));
      case VALID_UNTIL -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.validUntil));
      case CREATED_AT -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, coupon.createdAt));

      default ->
          throw new CustomException("PARTY#1_001", "정렬 기준이 잘못되었습니다.", HttpStatus.BAD_REQUEST);
    }
    return orderSpecifiers;
  }
}