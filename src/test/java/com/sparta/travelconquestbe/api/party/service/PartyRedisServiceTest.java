package com.sparta.travelconquestbe.api.party.service;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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

  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";

  @BeforeEach
  void setUp() {
    // Mockito 초기화
    MockitoAnnotations.openMocks(this);

    // RedisTemplate의 opsForValue()가 ValueOperations를 반환하도록 설정
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }
}