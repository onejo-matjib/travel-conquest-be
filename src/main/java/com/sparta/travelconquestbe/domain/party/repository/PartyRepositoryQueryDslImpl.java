package com.sparta.travelconquestbe.domain.party.repository;

import static com.sparta.travelconquestbe.domain.PartyTag.entity.QPartyTag.partyTag;
import static com.sparta.travelconquestbe.domain.coupon.entity.QCoupon.coupon;
import static com.sparta.travelconquestbe.domain.party.entity.QParty.party;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
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
public class PartyRepositoryQueryDslImpl implements PartyRepositoryQueryDsl {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<PartySearchResponse> searchAllPartise(Pageable pageable) {
    QueryResults<PartySearchResponse> results = jpaQueryFactory
        .select(Projections.constructor(PartySearchResponse.class,
            party.id,
            party.leaderNickname,
            party.name,
            party.description,
            party.count,
            party.countMax,
            party.status,
            party.passwordStatus,
            party.password,
            partyTag.tag.keyword,
            party.createdAt,
            party.updatedAt
        ))
        .from(party, partyTag)
        .leftJoin(party)
        .groupBy(party.id)
        .orderBy(getOrderSpecifiers(pageable.getSort()).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();
    List<PartySearchResponse> content = results.getResults();
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
        case "LEADER_NICKNAME" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, party.leaderNickname));
        case "NAME" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.name));
        case "COUNT_MAX" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.countMax));
        case "PASSWORD_STATUS" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, party.passwordStatus));
        case "STATUS" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.status));
        case "CREATED_AT" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.createdAt));

        // default = "CREATED_AT"
        default -> orderSpecifiers.add(new OrderSpecifier<>(direction, coupon.validUntil));
      }
    }
    return orderSpecifiers;
  }
}