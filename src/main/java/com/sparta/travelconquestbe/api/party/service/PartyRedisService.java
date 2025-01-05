package com.sparta.travelconquestbe.api.party.service;

import com.sparta.travelconquestbe.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartyRedisService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";

  public String generateRedisKey(Long partyId) {
    return PARTY_COUNT_KEY_PREFIX + partyId;
  }

  public int incrementPartyCount(Long partyId, int maxCount) {
    String key = PARTY_COUNT_KEY_PREFIX + partyId;

    try {
      // Redis에서 값 가져오기
      String countStr = redisTemplate.opsForValue().get(key);

      // 값이 없으면 초기화 (기본값 1로 설정)
      if (countStr == null) {
        redisTemplate.opsForValue().set(key, "1");
        return 1; // 기본값 1 반환
      }

      int currentCount = Integer.parseInt(countStr);

      // 최대치를 초과할 경우 예외 발생 전에 증가
      if (currentCount >= maxCount) {
        throw new CustomException("PARTY#4_001", "해당 파티의 인원수가 가득 찼습니다.", HttpStatus.CONFLICT);
      }

      // 값 증가 (원자적 증가 처리)
      int newCount = redisTemplate.opsForValue().increment(key).intValue();

      // 증가 후 다시 체크
      if (newCount > maxCount) {
        throw new CustomException("PARTY#4_001", "해당 파티의 인원수가 가득 찼습니다.", HttpStatus.CONFLICT);
      }

      return newCount;
    } catch (NumberFormatException e) {
      throw new CustomException("PARTY#4_002", "Redis 값이 유효하지 않습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new CustomException("PARTY#4_003", "Redis 값 증가 중 오류 발생",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  public void decrementPartyCount(Long partyId) {
    String redisKey = generateRedisKey(partyId);

    // 현재 Redis에서 파티 인원 수 조회
    String redisCountStr = redisTemplate.opsForValue().get(redisKey);
    if (redisCountStr == null) {
      throw new CustomException("PARTY#6_001", "파티의 인원 수를 조회할 수 없습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    int redisCount = Integer.parseInt(redisCountStr);

    // 인원 수가 1명일 경우 Redis에서 삭제
    if (redisCount == 1) {
      redisTemplate.delete(redisKey);
    } else {
      // 인원 수가 1명 이상일 때만 감소
      redisTemplate.opsForValue().decrement(redisKey, 1);
    }
  }

  public int getPartyCount(Long partyId) {
    String redisKey = generateRedisKey(partyId);
    String count = redisTemplate.opsForValue().get(redisKey);
    return count != null ? Integer.parseInt(count) : 1;
  }

  public void setPartyCount(Long partyId, int count) {
    String redisKey = generateRedisKey(partyId);
    redisTemplate.opsForValue().set(redisKey, String.valueOf(count));
  }

  public void deletePartyKey(Long partyId) {
    String redisKey = generateRedisKey(partyId);
    redisTemplate.delete(redisKey);
  }

  public int getPartyCountFromRedis(Long partyId) {
    String key = "party:count:" + partyId;

    // Redis에서 카운트를 가져오기
    String countStr = redisTemplate.opsForValue().get(key);

    if (countStr == null) {
      // 카운트가 없으면 기본값 1로 초기화
      redisTemplate.opsForValue().set(key, "1");
      return 1;
    }

    int currentCount = Integer.parseInt(countStr);

    // 현재 카운트가 1보다 작은 경우 예외 처리 (비즈니스 규칙 보장)
    if (currentCount < 1) {
      throw new CustomException("PARTY#4_003", "파티 카운트 값이 유효하지 않습니다.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return currentCount;
  }
}