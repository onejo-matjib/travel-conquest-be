package com.sparta.travelconquestbe.api.party.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartyRedisService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String PARTY_COUNT_KEY_PREFIX = "party:count:";

  // 파티 인원 수를 증가시킴
  public void incrementPartyCount(Long partyId) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    redisTemplate.opsForValue().increment(redisKey, 1);
  }

  // 파티 인원 수를 감소시킴
  public void decrementPartyCount(Long partyId) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    redisTemplate.opsForValue().decrement(redisKey, 1);
  }

  // Redis에서 파티 인원 수 가져오기
  public int getPartyCount(Long partyId) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    String count = redisTemplate.opsForValue().get(redisKey);
    return count != null ? Integer.parseInt(count) : 0; // Redis에 값이 없으면 0 반환
  }

  // Redis에 파티 인원 수 설정하기
  public void setPartyCount(Long partyId, int count) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    redisTemplate.opsForValue().set(redisKey, String.valueOf(count));
  }

}
