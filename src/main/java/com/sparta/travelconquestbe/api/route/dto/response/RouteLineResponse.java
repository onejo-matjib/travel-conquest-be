package com.sparta.travelconquestbe.api.route.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteLineResponse {
  private List<RouteDTO> routes;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class RouteDTO {
    private SummaryDTO summary;
    private List<SectionDTO> sections;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SummaryDTO {
    private int distance;
    private int duration;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class SectionDTO {
    private List<RoadDTO> roads;
    private List<GuideDTO> guides;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class RoadDTO {
    private String name;
    private int distance;
    private int duration;
    private Double traffic_speed;
    private int traffic_state;
    private Double[] vertexes;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class GuideDTO {
    private String name;
    private int distance;
    private int duration;
    private String guidance;
    private Double x;
    private Double y;
    private int type;
  }
}
