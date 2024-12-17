package com.sparta.travelconquestbe.api.routelocation.dto.respones;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LocationSearchResponse {
  private int sequence;
  private String locationName;
  private String mediaUrl;
}
