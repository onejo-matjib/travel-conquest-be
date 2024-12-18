package com.sparta.travelconquestbe.api.routelocation.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationDetailsRequest {
  private String origin;
  private String destination;

  @Builder
  public LocationDetailsRequest(String origin, String destination) {
    this.origin = origin;
    this.destination = destination;
  }
}
