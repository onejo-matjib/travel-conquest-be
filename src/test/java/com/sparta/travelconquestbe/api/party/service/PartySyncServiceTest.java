package com.sparta.travelconquestbe.api.party.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class PartySyncServiceTest {

  @Mock
  private PartyRepository partyRepository;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ListOperations<String, String> listOperations;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private PartySyncService partySyncService;

  private static final String MODIFIED_PARTIES_KEY = "modified_parties";
  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // RedisTemplate의 메서드를 Mock 설정
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void syncRedisToPartyDatabase_updatesPartyCountInDatabase() {
    // given
    Long partyId = 1L;
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    String redisCount = "25";

    Party party = Party.builder()
        .id(partyId)
        .name("Test Party")
        .count(0)
        .countMax(30)
        .build();

    // Redis에서 변경된 파티 ID 목록 설정
    when(listOperations.range(MODIFIED_PARTIES_KEY, 0, -1)).thenReturn(Arrays.asList("1"));
    when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
    when(valueOperations.get(redisKey)).thenReturn(redisCount); // Redis에서 반환되는 값을 설정

    // when
    partySyncService.syncRedisToPartyDatabase();

    // then
    assertThat(party.getCount()).isEqualTo(25); // count 값이 25로 업데이트되었는지 확인
    verify(partyRepository, times(1)).save(party); // save가 호출되었는지 확인
    verify(listOperations, times(1)).remove(MODIFIED_PARTIES_KEY, 1,
        partyId.toString()); // Redis 목록에서 제거 확인
  }

  @Test
  void syncRedisToPartyDatabase_skipsUpdateIfPartyNotFoundInDatabase() {
    // given
    Long partyId = 1L;
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;
    String redisCount = "25";

    when(listOperations.range(MODIFIED_PARTIES_KEY, 0, -1)).thenReturn(Arrays.asList("1"));
    when(partyRepository.findById(partyId)).thenReturn(Optional.empty()); // 파티가 DB에 없을 때
    when(valueOperations.get(redisKey)).thenReturn(redisCount);

    // when
    partySyncService.syncRedisToPartyDatabase();

    // then
    verify(partyRepository, never()).save(any(Party.class)); // 파티가 없으면 save 호출하지 않음
  }

  @Test
  void syncRedisToPartyDatabase_skipsUpdateIfRedisKeyNotFound() {
    // given
    Long partyId = 1L;
    String redisKey = PARTY_COUNT_KEY_PREFIX + partyId;

    Party party = Party.builder()
        .id(partyId)
        .name("Test Party")
        .count(0)
        .countMax(30)
        .build();

    when(listOperations.range(MODIFIED_PARTIES_KEY, 0, -1)).thenReturn(Arrays.asList("1"));
    when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
    when(valueOperations.get(redisKey)).thenReturn(null); // Redis에서 값이 없을 경우

    // when
    partySyncService.syncRedisToPartyDatabase();

    // then
    verify(partyRepository, never()).save(party); // Redis에서 값이 없으면 업데이트하지 않음
  }
}