package com.sparta.travelconquestbe.api.party.service;

import static com.sparta.travelconquestbe.domain.party.entity.QParty.party;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PartySyncService {

  private final PartyRepository partyRepository;
  private final PartyRedisService partyRedisService;
  private final RedisTemplate<String, String> redisTemplate;
  private final JPAQueryFactory jpaQueryFactory;

  // Redis 키 접두사와 변경된 파티 ID 저장용 리스트 키
  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";
  private static final String MODIFIED_PARTIES_KEY = "modified_parties";

  // 스레드 풀 생성 (예: 병렬 작업을 위해 4개의 스레드 사용)
  private final ExecutorService executorService = Executors.newFixedThreadPool(4);

  // 캐시된 파티 ID 리스트
  private List<Long> cachedPartyIds;

  public PartySyncService(PartyRepository partyRepository, PartyRedisService partyRedisService,
      RedisTemplate<String, String> redisTemplate, JPAQueryFactory jpaQueryFactory) {
    this.partyRepository = partyRepository;
    this.partyRedisService = partyRedisService;
    this.redisTemplate = redisTemplate;
    this.jpaQueryFactory = jpaQueryFactory;
    // 초기 캐싱
    this.cachedPartyIds = partyRepository.findAllPartyId();
  }

  // 5분마다 실행하여 Redis와 DB 동기화
  @Scheduled(fixedRate = 300000) // 5분마다 실행
  public void batchSyncRedisWithDatabase() {
    // Party ID와 해당 파티의 count를 한 번에 가져오기 위해 Tuple 사용
    List<Tuple> results = jpaQueryFactory
        .select(party.id, party.count)
        .from(party)
        .fetch(); // 모든 파티의 ID와 count를 가져옵니다.

    results.forEach(tuple -> {
      Long partyId = tuple.get(party.id);
      int dbCount = tuple.get(party.count);
      int redisCount = partyRedisService.getPartyCount(partyId);

      // Redis와 DB 값이 다르면 Redis 값을 업데이트
      if (redisCount != dbCount) {
        try {
          partyRedisService.setPartyCount(partyId, dbCount);
        } catch (Exception e) {
          // 동기화 실패 시 로그 기록
          log.error("Redis 동기화 실패: Party ID = {}", partyId, e);
        }
      }
    });
  }

  // 5분마다 실행하여 Redis에서 변경된 파티 ID를 DB에 반영
  @Scheduled(fixedRate = 300000) // 5분마다 실행
  public void syncRedisToPartyDatabase() {
    // Redis에서 변경된 파티 ID 가져오기
    List<Long> modifiedPartyIds = getModifiedPartyIdsFromRedis();

    // 변경된 파티 ID가 없으면 동기화 작업 중단
    if (modifiedPartyIds.isEmpty()) {
      return;
    }

    // 비동기 방식으로 동기화 작업 수행
    modifiedPartyIds.forEach(partyId -> {
      executorService.submit(() -> syncSingleParty(partyId)); // 병렬로 동기화
    });
  }

  // Redis에서 변경된 파티 ID 리스트 가져오기
  private List<Long> getModifiedPartyIdsFromRedis() {
    return redisTemplate.opsForList().range(MODIFIED_PARTIES_KEY, 0, -1)
        .stream()
        .map(Long::parseLong)
        .toList();
  }

  // 단일 파티 동기화 작업
  @Async
  @Transactional
  public void syncSingleParty(Long partyId) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    String redisCount = redisTemplate.opsForValue().get(redisKey);

    if (redisCount != null) {
      Party party = partyRepository.findById(partyId).orElse(null);
      if (party != null) {
        // DB와 Redis 동기화
        party.updateCount(Integer.parseInt(redisCount));
        partyRepository.save(party);

        // 동기화 완료 후 변경 리스트에서 제거
        redisTemplate.opsForList().remove(MODIFIED_PARTIES_KEY, 1, partyId.toString());
      }
    }
  }
}