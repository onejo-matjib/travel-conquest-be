package com.sparta.travelconquestbe.api.party.service;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.dto.response.PartyCreateResponse;
import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.PartyTag.entity.PartyTag;
import com.sparta.travelconquestbe.domain.PartyTag.repository.PartyTagRepository;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.enums.MemberType;
import com.sparta.travelconquestbe.domain.partyMember.repository.PartyMemberRepository;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import com.sparta.travelconquestbe.domain.tag.repository.TagRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyService {

  private final PartyRepository partyRepository;
  private final TagRepository tagRepository;
  private final PartyMemberRepository partyMemberRepository;
  private final UserRepository userRepository;
  private final PartyTagRepository partyTagRepository;

  public PartyCreateResponse createParty(AuthUserInfo userInfo, PartyCreateRequest request) {
    // Party 생성 및 저장
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
    partyRepository.save(party);

    // PartyMember 생성 및 저장
    User referenceUser = userRepository.getReferenceById(userInfo.getId());
    PartyMember partyMember = PartyMember.builder()
        .memberType(MemberType.LEADER)
        .user(referenceUser)
        .party(party)
        .build();
    partyMemberRepository.save(partyMember);

    // 해시태그 추출
    List<String> hashtags = extractHashtags(request.getTags());

    // 해시태그 처리
    processTags(hashtags, party);

    // 응답 객체 생성
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

  public Page<PartySearchResponse> searchAllPartise(Pageable pageable) {
    return partyRepository.searchAllPartise(pageable);
  }

  public void processTags(List<String> hashtags, Party party) {

    // 태그를 처리 및 저장
    List<Tag> tagList = hashtags.stream()
        .map(keyword -> tagRepository.findByKeyword(keyword)
            .orElseGet(() -> tagRepository.save(Tag.builder().keyword(keyword).build())))
        .collect(Collectors.toList());

    // PartyTag 생성 및 저장
    tagList.forEach(tag -> {
      PartyTag partyTag = PartyTag.builder()
          .party(party)
          .tag(tag)
          .build();
      partyTagRepository.save(partyTag);

    });
  }

  public static List<String> extractHashtags(String inputText) {
    if (inputText == null || inputText.isBlank()) {
      return List.of(); // Null 또는 빈 문자열일 경우 빈 리스트 반환
    }

    // 공백을 기준으로 문자열 분리
    return Arrays.stream(inputText.split("\\s+")) // 하나 이상의 공백을 기준으로 분리
        .map(String::trim) // 공백 제거
        .filter(tag -> !tag.isEmpty()) // 빈 태그 제거
        .filter(tag -> tag.startsWith("#")) // 해시태그 형식만 필터링
        .map(tag -> tag.substring(1)) // '#' 제외하고 태그 키워드만 추출
        .map(tag -> tag.replaceAll("[^a-zA-Z0-9가-힣]", "")) // 알파벳, 숫자, 한글만 남기기
        .filter(tag -> !tag.isEmpty()) // 최종적으로 빈 값 필터링
        .toList();
  }


}