package com.sparta.travelconquestbe.domain.coupon.repository;

import static com.sparta.travelconquestbe.domain.coupon.entity.QCoupon.coupon;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryQueryDslImpl implements CouponRepositoryQueryDsl {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<CouponSearchResponse> searchAllCoupons(Pageable pageable) {
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
        .orderBy(getOrderSpecifiers(pageable.getSort()).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<CouponSearchResponse> content = results.getResults();
    long totalCount = results.getTotal();

    return new PageImpl<>(content, pageable, totalCount);
  }

  private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    for (Sort.Order order : sort) {
      String property = order.getProperty();
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;

      // 특정 필드에 대한 매핑
      switch (property) {
        case "NAME" -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.name));
        case "TYPE" -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.type));
        case "COUNT" -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.count));
        case "DISCOUNT_AMOUNT" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.discountAmount));
        case "VALID_UNTIL" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.validUntil));
        case "CREATED_AT" -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.createdAt));

        // default = "VALID_UNTIL"
        default -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.validUntil));
      }
    }
    return orderSpecifiers;
  }
}