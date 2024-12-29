package com.sparta.travelconquestbe.api.party.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class PartyRedisServiceTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private PartyRedisService partyRedisService;

  private static final String PARTY_COUNT_KEY_PREFIX = "party:count:";

  @BeforeEach
  void setUp() {
    // Mockito 초기화
    MockitoAnnotations.openMocks(this);

    // RedisTemplate의 opsForValue()가 ValueOperations를 반환하도록 설정
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void testIncrementPartyCount_Success() {
    Long partyId = 1L;
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;

    // Mock behavior for get() and increment()
    when(valueOperations.get(redisKey)).thenReturn("2");
    when(valueOperations.increment(redisKey, 1)).thenReturn(3L);

    // Service call
    partyRedisService.incrementPartyCount(partyId);

    // Verify that increment() was called once
    verify(valueOperations, times(1)).increment(redisKey, 1);
  }
}