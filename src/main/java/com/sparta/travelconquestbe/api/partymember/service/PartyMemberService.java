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


  // 파티 탈퇴
  @Transactional
  public Void partyLeave(AuthUserInfo userInfo, Long partyId) {
    Party party = validatePartyMember(userInfo, partyId);  // 해당 유저가 파티의 멤버인지 확인
    PartyMember partyMember = partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(),
            partyId)
        .orElseThrow(
            () -> new CustomException("PARTY#3_002", "해당 파티의 맴버가 아닙니다.", HttpStatus.CONFLICT));

    // 리더 탈퇴 시 처리
    if (partyMember.getMemberType().equals(MemberType.LEADER)) {
      // 리더 탈퇴 시 새로운 리더 지정
      reassignPartyLeader(party);
    } else {

      // 파티에서 멤버 삭제
      partyMemberRepository.delete(partyMember);

      // Redis에서 인원 수 감소
      partyRedisService.decrementPartyCount(partyId);

      // 파티 상태 동기화: 파티 상태 업데이트 (인원 수에 맞게 상태 변경)
      syncPartyStatusToRedis(party);
      syncPartyStatusToDatabase(party);
    }
    return null;
  }

  public void reassignPartyLeader(Party party) {
    // 파티에 멤버가 있는지 확인 (리더를 제외한 멤버)
    List<PartyMember> remainingMembers = partyMemberRepository.findByPartyIdAndMemberTypeNot(
        party.getId(), MemberType.LEADER);

    if (remainingMembers.isEmpty()) {
      // 리더가 마지막 멤버인 경우, 파티 삭제
      deletePartyAndSync(party);
    } else {
      // 랜덤으로 새로운 리더 지정
      PartyMember newLeader = remainingMembers.get(new Random().nextInt(remainingMembers.size()));
      newLeader.chageMemberLeader(MemberType.LEADER);
      partyMemberRepository.save(newLeader);

      // 파티의 리더 닉네임 업데이트
      party.updateLeaderNickname(newLeader.getUser().getNickname());
      partyRepository.save(party);
    }
  }

  // 파티 삭제 및 DB, Redis 동기화
  public void deletePartyAndSync(Party party) {
    // 파티 삭제 전에 Redis에서 해당 파티 키 삭제
    String redisKey = PARTY_COUNT_KEY_PREFIX + party.getId();
    redisTemplate.delete(redisKey);

    // 파티 삭제
    partyMemberRepository.deletePartyMembersByPartyId(party.getId());
    partyRepository.delete(party);
  }

  private Party validatePartyMember(AuthUserInfo userInfo, Long id) {
    PartyMember partyMember = partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(), id)
        .orElseThrow(
            () -> new CustomException("PARTY#3_002", "해당 파티의 맴버가 아닙니다.",
                HttpStatus.CONFLICT));
    return partyMember.getParty();
  }

  // 파티 상태 Redis 동기화
  public void syncPartyStatusToRedis(Party party) {
    // Redis에서 해당 파티의 현재 인원 수 가져오기
    String redisKey = PARTY_COUNT_KEY_PREFIX + party.getId();
    String redisCount = redisTemplate.opsForValue().get(redisKey);

    if (redisCount != null) {
      // Redis 값이 있으면 인원 수 동기화
      party.updateCount(Integer.parseInt(redisCount)); // Redis 값으로 파티의 인원 수 동기화
    }

    // 파티 상태가 변경되었는지 확인
    if (party.getCount() >= party.getCountMax()) {
      party.updateStatus(PartyStatus.FULL); // 파티가 가득 찼다면 FULL 상태로 변경
    } else {
      party.updateStatus(PartyStatus.OPEN); // 파티가 가득 차지 않았다면 OPEN 상태 유지
    }

    // 파티 상태와 인원 수를 DB에 저장
    partyRepository.save(party);

    // 상태 변경 시 Redis에 반영
    redisTemplate.opsForValue().set(redisKey, String.valueOf(party.getCount()));
    // 상태 값도 Redis에 반영
    String statusKey = "party_status:" + party.getId();
    redisTemplate.opsForValue().set(statusKey, party.getStatus().name());
  }

  // 파티 상태 DB로 동기화
  public void syncPartyStatusToDatabase(Party party) {
    String redisKey = PARTY_COUNT_KEY_PREFIX + party.getId();
    String redisCount = redisTemplate.opsForValue().get(redisKey);

    if (redisCount != null) {
      party.updateCount(Integer.parseInt(redisCount)); // Redis 값으로 count 동기화
    }

    // 파티 상태 업데이트 (FULL 또는 OPEN)
    if (party.getCount() >= party.getCountMax()) {
      party.updateStatus(PartyStatus.FULL); // 인원 수가 가득 찼으면 FULL 상태
    } else {
      party.updateStatus(PartyStatus.OPEN); // 인원 수가 덜 차면 OPEN 상태
    }

    // 파티 상태 및 인원 수 업데이트
    partyRepository.save(party);

    // 상태 변경 시 Redis에 반영
    redisTemplate.opsForValue().set(redisKey, String.valueOf(party.getCount()));
    String statusKey = "party_status:" + party.getId();
    redisTemplate.opsForValue().set(statusKey, party.getStatus().name());
  }
}