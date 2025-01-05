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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartyMemberService {

  private final PartyMemberRepository partyMemberRepository;
  private final PartyRepository partyRepository;
  private final PartyRedisService partyRedisService;

  @Transactional
  public void partyLeave(AuthUserInfo userInfo, Long partyId) {
    Party party = partyRepository.findById(partyId)
        .orElseThrow(
            () -> new CustomException("PARTY#3_001", "존재하지 않는 파티입니다.", HttpStatus.NOT_FOUND));

    PartyMember partyMember = partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(),
            partyId)
        .orElseThrow(
            () -> new CustomException("PARTY#3_002", "파티에 속해있지 않습니다.", HttpStatus.CONFLICT));

    if (partyMember.getMemberType() == MemberType.LEADER) {
      // 리더가 마지막 멤버일 경우 파티 삭제
      if (partyRedisService.getPartyCount(partyId) <= 1) {
        deleteParty(party); // 파티 삭제 로직을 공통화
      } else {
        reassignPartyLeader(party); // 리더 재배정
        partyRedisService.decrementPartyCount(partyId);
        partyMemberRepository.delete(partyMember);
      }
    } else {
      // 일반 멤버 탈퇴
      partyRedisService.decrementPartyCount(partyId);
      partyMemberRepository.delete(partyMember);
    }

    // Redis 동기화
    try {
      syncRedisWithDatabase(partyId);
    } catch (Exception e) {
      throw new CustomException("PARTY#3_003", "Redis 동기화 중 오류 발생.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void reassignPartyLeader(Party party) {
    List<PartyMember> remainingMembers = partyMemberRepository.findByPartyIdAndMemberTypeNot(
        party.getId(), MemberType.LEADER);

    if (remainingMembers.isEmpty()) {
      // 리더가 마지막 멤버인 경우 파티 삭제
      deleteParty(party);
    } else {
      // 새로운 리더 지정
      PartyMember newLeader = remainingMembers.get(new Random().nextInt(remainingMembers.size()));
      newLeader.chageMemberLeader(MemberType.LEADER);
      partyMemberRepository.save(newLeader);

      party.updateLeaderNickname(newLeader.getUser().getNickname());
      partyRepository.save(party);
    }
  }

  public void deleteParty(Party party) {
    partyRedisService.deletePartyKey(party.getId());
    partyMemberRepository.deletePartyMembersByPartyId(party.getId());
    partyRepository.delete(party);
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