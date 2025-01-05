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
import com.sparta.travelconquestbe.api.party.service.PartyRedisService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import com.sparta.travelconquestbe.domain.partyMember.entity.QPartyMember;
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
public class PartyRepositoryQueryDslImpl implements PartyRepositoryQueryDsl {

  private final JPAQueryFactory jpaQueryFactory;
  private final PartyRedisService partyRedisService;

  @Override
  public Page<PartySearchResponse> searchAllPartise(Pageable pageable, PartySort partySort,
      String direction) {
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
        .orderBy(getOrderSpecifiers(partySort, direction).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    // Redis 동기화는 서비스 계층에서 수행
    return mapToPartySearchResponse(results.getResults(), pageable, results.getTotal());
  }

  @Override
  public Page<PartySearchResponse> searchAllMyPartise(
      Long userId,
      Pageable pageable,
      PartySort partySort,
      String direction
  ) {
    QPartyMember partyMemberAlias = QPartyMember.partyMember;

    // 기본 파티 데이터 조회
    List<Tuple> results = jpaQueryFactory
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
        .leftJoin(party.partyMember, partyMemberAlias)
        .where(partyMemberAlias.user.id.eq(userId))  // 탈퇴한 사용자는 party_member에 없으므로 자동 필터링
        .groupBy(
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
        .orderBy(getOrderSpecifiers(partySort, direction).toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // Redis 동기화 및 파티 데이터 매핑
    List<PartySearchResponse> content = results.stream().map(tuple -> {
      Long partyId = tuple.get(party.id);

      // Redis에서 인원 수 가져오기
      int count = partyRedisService.getPartyCount(partyId);
      if (count == 0) {
        count = tuple.get(party.count); // Redis 값이 없을 경우 DB 값 사용
        partyRedisService.setPartyCount(partyId, count); // Redis에 동기화
      }

      return PartySearchResponse.builder()
          .id(partyId)
          .leaderNickname(tuple.get(party.leaderNickname))
          .name(tuple.get(party.name))
          .description(tuple.get(party.description))
          .count(count)
          .countMax(tuple.get(party.countMax))
          .status(tuple.get(party.status))
          .passwordStatus(tuple.get(party.passwordStatus))
          .createdAt(tuple.get(party.createdAt))
          .updatedAt(tuple.get(party.updatedAt))
          .build();
    }).toList();

    // 전체 파티 수 조회
    long totalCount = jpaQueryFactory
        .select(party.count())
        .from(party)
        .leftJoin(party.partyMember, partyMemberAlias)
        .where(partyMemberAlias.user.id.eq(userId))
        .fetchOne();

    return new PageImpl<>(content, pageable, totalCount);
  }

  private Page<PartySearchResponse> mapToPartySearchResponse(List<Tuple> results, Pageable pageable,
      long totalCount) {
    List<PartySearchResponse> content = results.stream().map(tuple -> {
      Long partyId = tuple.get(party.id);

      // Redis에서 카운트 가져오기
      int count = partyRedisService.getPartyCount(partyId);

      // Redis에 값이 없으면 DB 값 사용 및 동기화
      if (count == 1) {
        count = tuple.get(party.count);
        partyRedisService.setPartyCount(partyId, count);
      }

      // 태그 가져오기
      List<String> tags = getPartyTags(partyId);

      return PartySearchResponse.builder()
          .id(partyId)
          .leaderNickname(tuple.get(party.leaderNickname))
          .name(tuple.get(party.name))
          .description(tuple.get(party.description))
          .count(count) // 최신 카운트 값 사용
          .countMax(tuple.get(party.countMax))
          .status(tuple.get(party.status))
          .passwordStatus(tuple.get(party.passwordStatus))
          .tags(tags)
          .createdAt(tuple.get(party.createdAt))
          .updatedAt(tuple.get(party.updatedAt))
          .build();
    }).toList();

    return new PageImpl<>(content, pageable, totalCount);
  }


  private List<String> getPartyTags(Long partyId) {
    return jpaQueryFactory.select(tag.keyword)
        .from(partyTag)
        .join(partyTag.tag, tag)
        .where(partyTag.party.id.eq(partyId))
        .fetch();
  }

  // 다중 정렬 조건
  private List<OrderSpecifier<?>> getOrderSpecifiers(PartySort partySort, String direction) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    // 정렬 방향 설정 (기본값: DESC)
    Order sortOrder = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

    switch (partySort) {
      case LEADER_NICKNAME ->
          orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.leaderNickname));
      case NAME -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.name));
      case COUNT_MAX -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.countMax));
      case PASSWORD_STATUS ->
          orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.passwordStatus));
      case STATUS -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.status));
      case CREATED_AT -> orderSpecifiers.add(new OrderSpecifier<>(sortOrder, party.createdAt));
      default ->
          throw new CustomException("PARTY#1_001", "정렬 기준이 잘못되었습니다.", HttpStatus.BAD_REQUEST);
    }
    return orderSpecifiers;
  }
}