package com.sparta.travelconquestbe.api.party.service;

import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartySyncService {

  private final PartyRepository partyRepository;
  private final RedisTemplate<String, String> redisTemplate;

  // Redis 키 접두사와 변경된 파티 ID 저장용 리스트 키
  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";
  private static final String MODIFIED_PARTIES_KEY = "modified_parties";

  // 스레드 풀 생성 (예: 병렬 작업을 위해 4개의 스레드 사용)
  private final ExecutorService executorService = Executors.newFixedThreadPool(4);

  // 캐시된 파티 ID 리스트
  private List<Long> cachedPartyIds;

  public PartySyncService(PartyRepository partyRepository,
      RedisTemplate<String, String> redisTemplate) {
    this.partyRepository = partyRepository;
    this.redisTemplate = redisTemplate;
    // 초기 캐싱
    this.cachedPartyIds = partyRepository.findAllPartyId();
  }

  // 5분마다 실행
  @Scheduled(fixedRate = 300000)
  public void syncRedisToPartyDatabase() {
    // Redis에서 변경된 파티 ID 가져오기
    List<Long> modifiedPartyIds = getModifiedPartyIdsFromRedis();

    // 변경된 파티 ID가 없으면 동기화 작업 중단
    if (modifiedPartyIds.isEmpty()) {
      return;
    }

    // 비동기 방식으로 동기화 작업 수행
    for (Long partyId : modifiedPartyIds) {
      syncSingleParty(partyId); // @Async가 자동으로 비동기 처리
    }
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
        party.updateCount(Integer.parseInt(redisCount));
        partyRepository.save(party);

        // 동기화 완료 후 변경 리스트에서 제거
        redisTemplate.opsForList().remove(MODIFIED_PARTIES_KEY, 1, partyId.toString());
      }
    }
  }

  // 새로운 파티 추가 시 캐시 갱신
  public void addPartyToCache(Long newPartyId) {
    cachedPartyIds.add(newPartyId);
  }

  // 파티 삭제 시 캐시 갱신
  public void removePartyFromCache(Long partyIdToRemove) {
    cachedPartyIds.remove(partyIdToRemove);
  }

  // Redis 데이터 변경 이벤트 처리 (예: 파티 데이터 변경 시 호출)
  public void markPartyAsModified(Long partyId) {
    redisTemplate.opsForList().rightPush(MODIFIED_PARTIES_KEY, partyId.toString());
  }
}