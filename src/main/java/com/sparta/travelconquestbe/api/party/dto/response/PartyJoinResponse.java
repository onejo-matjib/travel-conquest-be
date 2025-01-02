package com.sparta.travelconquestbe.api.party.dto.response;

import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartyJoinResponse {

  private Long id;
  private String leaderNickname;
  private String name;
  private String description;
  private int count;
  private int countMax;
  private boolean passwordStatus;
  private PartyStatus status;
  private LocalDateTime createdAt;
}