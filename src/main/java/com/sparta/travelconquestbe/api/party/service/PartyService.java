package com.sparta.travelconquestbe.api.party.service;

import com.sparta.travelconquestbe.api.party.dto.PartyCreateReseponse;
import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyService {

  private final PartyRepository partyRepository;

  public PartyCreateReseponse createParty(AuthUserInfo userInfo, PartyCreateRequest request) {
    // 태그 문제 해결해야 함
    List<Tag> requestTags = toList(request);

    Party party = Party.builder().
        id(request.getId())
        .name(request.getName())
        .description(request.getDescription())
        .count(request.getCount())
        .count(request.getCount())
        .countMax(request.getCountMax())
        .passwordStatus(request.isPasswordStatus())
        .password(request.getPassword())
        .tags(requestTags)
        .build();

    partyRepository.save(party);
    PartyCreateReseponse reseponse = PartyCreateReseponse.builder()
        .id(party.getId())
        .leaderId(userInfo.getId())
        .leaderNickname(userInfo.getNickname())
        .name(party.getName())
        .description(party.getDescription())
        .count(party.getCount())
        .countMax(party.getCountMax())
        .passwordStatus(party.isPasswordStatus())
        .password(party.getPassword())
        .status(PartyStatus.OPEN)
        .createdAt(party.getCreatedAt())
        .updatedAt(party.getUpdatedAt())
        .build();
    return reseponse;
  }

  // String 형태의 tags를 List<Tag>로 변환하는 메서드
  public List<Tag> toList(PartyCreateRequest request) {
    if (request.getTags() == null || request.getTags().isEmpty()) {
      return List.of(); // 빈 리스트 반환
    }

    return Arrays.stream(request.getTags().split(","))
        .map(String::trim) // 공백 제거
        .map(tagName -> Tag.builder().name(tagName).build()) // Tag 객체 생성
        .collect(Collectors.toList());
  }
}