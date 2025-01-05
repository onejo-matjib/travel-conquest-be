package com.sparta.travelconquestbe.api.partymember.service;

import com.sparta.travelconquestbe.api.party.service.PartyRedisService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.enums.MemberType;
import com.sparta.travelconquestbe.domain.partyMember.repository.PartyMemberRepository;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartyMemberService {

  private final PartyMemberRepository partyMemberRepository;
  private final PartyRepository partyRepository;
  private final PartyRedisService partyRedisService;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";

  @Transactional
  public void partyLeave(AuthUserInfo userInfo, Long partyId) {
    // 1. Party 및 PartyMember 조회
    Party party = partyRepository.findById(partyId)
        .orElseThrow(
            () -> new CustomException("PARTY#3_001", "존재하지 않는 파티입니다.", HttpStatus.NOT_FOUND));

    PartyMember partyMember = partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(),
            partyId)
        .orElseThrow(
            () -> new CustomException("PARTY#3_002", "파티에 속해있지 않습니다.", HttpStatus.CONFLICT));

    // 2. 리더 여부 확인
    if (partyMember.getMemberType() == MemberType.LEADER) {
      if (partyRedisService.getPartyCount(partyId) == 1) {
        // 마지막 멤버일 경우: PartyMember 및 Party 삭제
        deletePartyAndSync(party);
      } else {
        // 리더 재배정
        reassignPartyLeader(party);
        partyRedisService.decrementPartyCount(partyId);
        partyMemberRepository.delete(partyMember);

        // Redis 동기화
        syncRedisSafely(partyId);
      }
    } else {
      // 일반 멤버 탈퇴
      partyRedisService.decrementPartyCount(partyId);
      partyMemberRepository.delete(partyMember);

      // Redis 동기화
      syncRedisSafely(partyId);
    }
  }

  // Redis 동기화를 안전하게 처리하는 메서드
  private void syncRedisSafely(Long partyId) {
    try {
      syncRedisWithDatabase(partyId);
    } catch (Exception e) {
      throw new CustomException("PARTY#3_003", "Redis 동기화 중 오류 발생.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Transactional
  public void deletePartyAndSync(Party party) {
    partyMemberRepository.deletePartyMembersByPartyId(party.getId());
    partyRedisService.deletePartyKey(party.getId());
    partyRepository.delete(party);
  }

  public void reassignPartyLeader(Party party) {
    List<PartyMember> remainingMembers = partyMemberRepository.findByPartyIdAndMemberTypeNot(
        party.getId(), MemberType.LEADER);

    if (remainingMembers.isEmpty()) {
      // 리더가 마지막 멤버인 경우 파티 삭제
      partyMemberRepository.deletePartyMembersByPartyId(party.getId());
      partyRedisService.deletePartyKey(party.getId());
      partyRepository.delete(party);
    } else {
      // 새로운 리더 지정
      PartyMember newLeader = remainingMembers.get(new Random().nextInt(remainingMembers.size()));
      newLeader.chageMemberLeader(MemberType.LEADER);
      partyMemberRepository.save(newLeader);

      // Party 업데이트
      party.updateLeaderNickname(newLeader.getUser().getNickname());
      partyRepository.save(party);
    }
  }


  public Party validatePartyMember(AuthUserInfo userInfo, Long partyId) {
    return partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(), partyId)
        .orElseThrow(() -> new CustomException("PARTY#3_002", "파티 멤버가 아닙니다.", HttpStatus.CONFLICT))
        .getParty();
  }

  @Transactional
  public void syncRedisWithDatabase(Long partyId) {
    // Redis에서 현재 카운트 값 가져오기
    int redisCount = partyRedisService.getPartyCount(partyId);
    Party party = partyRepository.findById(partyId).orElseThrow(() ->
        new CustomException("해당 파티가 존재하지 않습니다.", "PARTY#2_001", HttpStatus.NOT_FOUND));

    party.updateCount(redisCount);

    // 파티 상태 업데이트
    if (redisCount < party.getCountMax()) {
      party.updateStatus(PartyStatus.OPEN);
    }

    // DB 저장
    partyRepository.save(party);
  }
}