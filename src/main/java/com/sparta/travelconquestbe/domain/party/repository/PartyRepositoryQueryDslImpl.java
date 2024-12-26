package com.sparta.travelconquestbe.domain.party.repository;

import static com.sparta.travelconquestbe.domain.PartyTag.entity.QPartyTag.partyTag;
import static com.sparta.travelconquestbe.domain.party.entity.QParty.party;
import static com.sparta.travelconquestbe.domain.tag.entity.QTag.tag;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
    // Party 데이터 쿼리
    QueryResults<Tuple> results = jpaQueryFactory
        .select(
            party.id,
            party.leaderNickname,
            party.name,
            party.description,
            party.count,
            party.countMax,
            party.status,
            party.passwordStatus,
            party.createdAt,
            party.updatedAt
        )
        .from(party)
        .orderBy(getOrderSpecifiers(pageable.getSort()).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    // tags를 별도로 처리하고 DTO 빌더로 매핑
    List<PartySearchResponse> content = results.getResults().stream().map(tuple -> {
      Long partyId = tuple.get(party.id);

      // 태그 리스트 조회
      List<String> tags = jpaQueryFactory.select(tag.keyword)
          .from(partyTag)
          .join(partyTag.tag, tag)
          .where(partyTag.party.id.eq(partyId))
          .fetch();

      // DTO 생성
      return PartySearchResponse.builder()
          .id(partyId)
          .leaderNickname(tuple.get(party.leaderNickname))
          .name(tuple.get(party.name))
          .description(tuple.get(party.description))
          .count(tuple.get(party.count))
          .countMax(tuple.get(party.countMax))
          .status(tuple.get(party.status))
          .passwordStatus(tuple.get(party.passwordStatus))
          .tags(tags) // 태그 리스트 추가
          .createdAt(tuple.get(party.createdAt))
          .updatedAt(tuple.get(party.updatedAt))
          .build();
    }).toList();

    long totalCount = results.getTotal();
    return new PageImpl<>(content, pageable, totalCount);
  }


  private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    for (Sort.Order order : sort) {
      String property = order.getProperty();
      Order direction = order.isAscending() ? Order.ASC : Order.DESC;

      // 특정 필드에 대한 매핑
      switch (property.toUpperCase()) {
        case "LEADER_NICKNAME" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, party.leaderNickname));
        case "NAME" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.name));
        case "COUNT_MAX" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.countMax));
        case "PASSWORD_STATUS" ->
            orderSpecifiers.add(new OrderSpecifier<>(direction, party.passwordStatus));
        case "STATUS" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.status));
        case "CREATED_AT" -> orderSpecifiers.add(new OrderSpecifier<>(direction, party.createdAt));
        default -> throw new IllegalArgumentException("Invalid sort property: " + property);
      }
    }
    return orderSpecifiers;
  }
}