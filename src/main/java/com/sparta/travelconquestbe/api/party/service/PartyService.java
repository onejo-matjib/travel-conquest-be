package com.sparta.travelconquestbe.api.party.service;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.dto.request.PartyUpdateRequest;
import com.sparta.travelconquestbe.api.party.dto.response.PartyCreateResponse;
import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import com.sparta.travelconquestbe.api.party.dto.response.PartyUpdateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.PartyTag.entity.PartyTag;
import com.sparta.travelconquestbe.domain.PartyTag.repository.PartyTagRepository;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.enums.MemberType;
import com.sparta.travelconquestbe.domain.partyMember.repository.PartyMemberRepository;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import com.sparta.travelconquestbe.domain.tag.repository.TagRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartyService {

  private final PartyRepository partyRepository;
  private final TagRepository tagRepository;
  private final PartyMemberRepository partyMemberRepository;
  private final UserRepository userRepository;
  private final PartyTagRepository partyTagRepository;

  // 파티 생성
  public PartyCreateResponse createParty(AuthUserInfo userInfo, PartyCreateRequest request) {
    validateUser(userInfo);

    Party party = createAndSaveParty(userInfo, request);
    addPartyLeader(userInfo, party);
    List<String> hashtags = extractHashtags(request.getTags());
    processTags(hashtags, party);

    return buildCreateResponse(userInfo, party, hashtags);
  }

  // 파티 전체 조회
  public Page<PartySearchResponse> searchAllPartise(Pageable pageable, PartySort partySort,
      String direction) {
    return partyRepository.searchAllPartise(pageable, partySort, direction);
  }

  // 파티 수정
  @Transactional
  public PartyUpdateResponse updateParty(AuthUserInfo userInfo, Long id,
      PartyUpdateRequest request) {
    Party party = validatePartyLeader(userInfo, id);

    validatePartyUpdateRequest(request, party);
    updatePartyStatus(party, request);

    List<String> existingTags = getExistingTags(party);
    List<String> newTags = extractHashtags(request.getTags());
    List<String> tagsToAdd = filterNewTags(existingTags, newTags);
    processTags(tagsToAdd, party);

    List<String> allTags = mergeTags(existingTags, tagsToAdd);

    return buildUpdateResponse(userInfo, party, allTags);
  }

  public PartyCreateResponse buildCreateResponse(AuthUserInfo userInfo, Party party,
      List<String> hashtags) {
    return PartyCreateResponse.builder()
        .id(party.getId())
        .leaderId(userInfo.getId())
        .leaderNickname(userInfo.getNickname())
        .name(party.getName())
        .description(party.getDescription())
        .count(party.getCount())
        .countMax(party.getCountMax())
        .passwordStatus(party.isPasswordStatus())
        .password(party.getPassword())
        .status(party.getStatus())
        .tags(hashtags)
        .createdAt(party.getCreatedAt())
        .updatedAt(party.getUpdatedAt())
        .build();
  }

  public PartyUpdateResponse buildUpdateResponse(AuthUserInfo userInfo, Party party,
      List<String> allTags) {
    return PartyUpdateResponse.builder()
        .id(party.getId())
        .leaderId(userInfo.getId())
        .leaderNickname(userInfo.getNickname())
        .name(party.getName())
        .description(party.getDescription())
        .count(party.getCount())
        .countMax(party.getCountMax())
        .passwordStatus(party.isPasswordStatus())
        .password(party.getPassword())
        .status(party.getStatus())
        .tags(allTags)
        .createdAt(party.getCreatedAt())
        .updatedAt(party.getUpdatedAt())
        .build();
  }

  public Party createAndSaveParty(AuthUserInfo userInfo, PartyCreateRequest request) {
    Party party = Party.builder()
        .leaderNickname(userInfo.getNickname())
        .name(request.getName())
        .description(request.getDescription())
        .count(1)
        .countMax(request.getCountMax())
        .status(PartyStatus.OPEN)
        .passwordStatus(request.isPasswordStatus())
        .password(request.getPassword())
        .build();
    return partyRepository.save(party);
  }

  public void addPartyLeader(AuthUserInfo userInfo, Party party) {
    User referenceUser = userRepository.getReferenceById(userInfo.getId());
    PartyMember partyMember = PartyMember.builder()
        .memberType(MemberType.LEADER)
        .user(referenceUser)
        .party(party)
        .build();
    partyMemberRepository.save(partyMember);
  }

  /**
   * 태그 관련 로직
   */

  // 태그 저장
  public void processTags(List<String> hashtags, Party party) {
    List<Tag> tagList = hashtags.stream()
        .map(keyword -> tagRepository.findByKeyword(keyword)
            .orElseGet(() -> tagRepository.save(Tag.builder().keyword(keyword).build())))
        .collect(Collectors.toList());

    tagList.forEach(tag -> partyTagRepository.save(
        PartyTag.builder().party(party).tag(tag).build()));
  }

  // 해시태그 추출
  public static List<String> extractHashtags(String inputText) {
    if (inputText == null || inputText.isBlank()) {
      return List.of();
    }
    return Arrays.stream(inputText.split("\\s+"))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .filter(tag -> tag.startsWith("#"))
        .map(tag -> tag.substring(1).replaceAll("[^a-zA-Z0-9가-힣]", ""))
        .filter(tag -> !tag.isEmpty())
        .toList();
  }

  public List<String> getExistingTags(Party party) {
    return party.getPartyTags().stream()
        .map(partyTag -> partyTag.getTag().getKeyword())
        .toList();
  }

  public List<String> filterNewTags(List<String> existingTags, List<String> newTags) {
    return newTags.stream()
        .filter(tag -> !existingTags.contains(tag))
        .toList();
  }

  public List<String> mergeTags(List<String> existingTags, List<String> newTags) {
    return Stream.concat(existingTags.stream(), newTags.stream())
        .distinct()
        .toList();
  }

  /**
   * 검증 로직
   */

  // 사용자 검증
  public void validateUser(AuthUserInfo userInfo) {
    if (userInfo.getType().equals(UserType.USER)) {
      throw new CustomException("PARTY#3_001", "인증된 사용자가 아닙니다.", HttpStatus.FORBIDDEN);
    }
  }

  // 파티 리더 검증
  public Party validatePartyLeader(AuthUserInfo userInfo, Long id) {
    Party party = partyRepository.findById(id)
        .orElseThrow(
            () -> new CustomException("PARTY#2_001", "해당 파티를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    PartyMember partyMember = partyMemberRepository.findByUserIdAndPartyId(userInfo.getId(), id)
        .orElseThrow(
            () -> new CustomException("PARTY#4_001", "해당 파티 멤버가 아닙니다.", HttpStatus.CONFLICT));

    if (!partyMember.getMemberType().equals(MemberType.LEADER)) {
      throw new CustomException("PARTY#3_001", "해당 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    return party;
  }

  /**
   * 파티 업데이트
   */

  public void validatePartyUpdateRequest(PartyUpdateRequest request, Party party) {
    if (request.getCountMax() < party.getCount()) {
      throw new CustomException("PARTY#1_001", "최대 인원 수가 현재 인원보다 낮습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  // 업데이트 이후 방 status 변화
  public void updatePartyStatus(Party party, PartyUpdateRequest request) {
    party.update(request.getName(), request.getDescription(), request.getCountMax(),
        request.isPasswordStatus(), request.getPassword());
    if (request.getCountMax() == party.getCount()) {
      party.updateStatus(PartyStatus.FULL);
    } else {
      party.updateStatus(PartyStatus.OPEN);
    }
  }
}