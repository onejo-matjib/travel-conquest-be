package com.sparta.travelconquestbe.domain.mycoupon.repository;

import static com.sparta.travelconquestbe.domain.coupon.entity.QCoupon.coupon;
import static com.sparta.travelconquestbe.domain.mycoupon.entity.QMyCoupon.myCoupon;
import static com.sparta.travelconquestbe.domain.user.entity.QUser.user;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.mycoupon.dto.response.MyCouponListResponse;
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
public class MyCouponRepositoryQueryDslImpl implements MyCouponRepositoryQueryDsl {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<MyCouponListResponse> searchAllMyCoupons(Long userId, Pageable pageable) {
    QueryResults<MyCouponListResponse> results = jpaQueryFactory
        .select(Projections.constructor(MyCouponListResponse.class,
            myCoupon.id,
            myCoupon.coupon.name,
            myCoupon.coupon.description,
            myCoupon.coupon.type,
            myCoupon.code,
            myCoupon.status,
            myCoupon.coupon.discountAmount,
            myCoupon.coupon.validUntil,
            myCoupon.createdAt
        ))
        .from(myCoupon)
        .leftJoin(myCoupon.coupon, coupon)
        .leftJoin(myCoupon.user, user)
        .where(
            myCoupon.user.id.eq(userId)
        )
        .groupBy(myCoupon.id)
        .orderBy(getOrderSpecifiers(pageable.getSort()).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<MyCouponListResponse> content = results.getResults();
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
        case "NAME" -> orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.coupon.name));
        case "TYPE" -> orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.coupon.type));
        case "STATUS" -> orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.status));
        case "DISCOUNT_AMOUNT" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.coupon.discountAmount));
        case "VALID_UNTIL" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.coupon.validUntil));
        case "CREATED_AT" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.createdAt));

        // default = "CREATED_AT"
        default -> orderSpecifiers.add(new OrderSpecifier<>(direction, myCoupon.createdAt));
      }
    }
    return orderSpecifiers;
  }
}