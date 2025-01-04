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
import com.sparta.travelconquestbe.domain.PartyTag.entity.QPartyTag;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import com.sparta.travelconquestbe.domain.partyMember.entity.QPartyMember;
import com.sparta.travelconquestbe.domain.tag.entity.QTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    List<PartySearchResponse> content = results.getResults().stream().map(tuple -> {
      Long partyId = tuple.get(party.id);

      // Redis에서 인원 수 가져오기
      int count = partyRedisService.getPartyCount(partyId);
      if (count == 0) {
        // Redis에 값이 없으면 DB에서 가져오고 Redis에 저장
        count = tuple.get(party.count);
        partyRedisService.setPartyCount(partyId, count);
      }

      // 태그 리스트 조회
      List<String> tags = jpaQueryFactory.select(tag.keyword)
          .from(partyTag)
          .join(partyTag.tag, tag)
          .where(partyTag.party.id.eq(partyId))
          .fetch();

      return PartySearchResponse.builder()
          .id(partyId)
          .leaderNickname(tuple.get(party.leaderNickname))
          .name(tuple.get(party.name))
          .description(tuple.get(party.description))
          .count(count)  // Redis 값으로 count 설정
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

  @Override
  public Page<PartySearchResponse> searchAllMyPartise(
      Long userId,
      Pageable pageable,
      PartySort partySort,
      String direction
  ) {
    QPartyMember partyMemberAlias = QPartyMember.partyMember;
    QPartyTag partyTagAlias = QPartyTag.partyTag;
    QTag tagAlias = QTag.tag;

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
        .where(partyMemberAlias.user.id.eq(userId))
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

    // 태그 데이터 조회 및 그룹화
    Map<Long, List<String>> tagMap = jpaQueryFactory
        .select(partyTag.party.id, tagAlias.keyword)
        .from(partyTag)
        .join(partyTag.tag, tagAlias)
        .where(partyTag.party.id.in(results.stream().map(r -> r.get(party.id)).toList()))
        .fetch()
        .stream()
        .collect(Collectors.groupingBy(
            tuple -> tuple.get(partyTag.party.id),
            Collectors.mapping(tuple -> tuple.get(tagAlias.keyword), Collectors.toList())
        ));

    // 결과 매핑
    List<PartySearchResponse> content = results.stream()
        .map(tuple -> PartySearchResponse.builder()
            .id(tuple.get(party.id))
            .leaderNickname(tuple.get(party.leaderNickname))
            .name(tuple.get(party.name))
            .description(tuple.get(party.description))
            .count(tuple.get(party.count))
            .countMax(tuple.get(party.countMax))
            .status(tuple.get(party.status))
            .passwordStatus(tuple.get(party.passwordStatus))
            .tags(tagMap.get(tuple.get(party.id))) // 태그 추가
            .createdAt(tuple.get(party.createdAt))
            .updatedAt(tuple.get(party.updatedAt))
            .build())
        .toList();

    long totalCount = jpaQueryFactory
        .select(party.count())
        .from(party)
        .leftJoin(party.partyMember, partyMemberAlias)
        .where(partyMemberAlias.user.id.eq(userId))
        .fetchOne();

    return new PageImpl<>(content, pageable, totalCount);
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