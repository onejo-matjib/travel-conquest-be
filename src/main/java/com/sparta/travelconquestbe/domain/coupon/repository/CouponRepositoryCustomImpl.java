package com.sparta.travelconquestbe.domain.coupon.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.travelconquestbe.domain.coupon.entity.QCoupon.coupon;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryCustomImpl implements CouponRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<CouponSearchResponse> searchAllCoupons(Pageable pageable) {

        QueryResults<CouponSearchResponse> results = jpaQueryFactory
                .select(Projections.constructor(CouponSearchResponse.class,
                        coupon.id,
                        coupon.name,
                        coupon.description,
                        coupon.code,
                        coupon.discountAmount,
                        coupon.validUntil,
                        coupon.count,
                        coupon.createdAt,
                        coupon.updatedAt
                ))
                .from(coupon)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<CouponSearchResponse> content = results.getResults();
        long totalCount = results.getTotal();

        return new PageImpl<>(content, pageable, totalCount);
    }
}
