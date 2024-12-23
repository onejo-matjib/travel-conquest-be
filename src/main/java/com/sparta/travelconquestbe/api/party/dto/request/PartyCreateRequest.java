package com.sparta.travelconquestbe.api.party.dto.request;

import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import lombok.Getter;

@Getter
public class PartyCreateRequest {

  private Long id;
  private Long leaderId;
  private String leaderNickname;
  private String name;
  private String description;
  private int count;
  private int countMax;
  private boolean passwordStatus;
  private String password;
  private String tags;
  private PartyStatus status;
}