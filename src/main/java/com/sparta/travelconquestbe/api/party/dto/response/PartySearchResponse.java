package com.sparta.travelconquestbe.api.party.dto.response;

import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartySearchResponse {

  private Long id;
  private String leaderNickname;
  private String name;
  private String description;
  private int count;
  private int countMax;
  private boolean passwordStatus;
  private String password;
  private List<String> tags;
  private PartyStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}