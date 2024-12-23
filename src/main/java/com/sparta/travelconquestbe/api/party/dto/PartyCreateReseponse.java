package com.sparta.travelconquestbe.api.party.dto;

import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PartyCreateReseponse {

  private Long id;
  private Long leaderId;
  private String leaderNickname;
  private String name;
  private String description;
  private int count;
  private int countMax;
  private boolean passwordStatus;
  private String password;
  private List<Tag> tags;
  private PartyStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}