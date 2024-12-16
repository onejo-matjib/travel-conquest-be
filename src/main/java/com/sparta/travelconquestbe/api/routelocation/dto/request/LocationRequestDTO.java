package com.sparta.travelconquestbe.api.routelocation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class LocationRequestDTO {
  @NotNull private Origin origin;

  @NotNull private Destination destination;

  private List<Waypoint> waypoints;

  @Builder
  public LocationRequestDTO(Origin origin, Destination destination, List<Waypoint> waypoints) {
    this.origin = origin;
    this.destination = destination;
    this.waypoints = waypoints;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @Builder
  public static class Origin {
    @NotNull private Double x;
    @NotNull private Double y;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @Builder
  public static class Destination {
    @NotNull private Double x;
    @NotNull private Double y;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @Builder
  public static class Waypoint {
    private String name;
    @NotNull private Double x;
    @NotNull private Double y;
  }
}
