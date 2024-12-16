package com.sparta.travelconquestbe.api.route.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteLineResponse {
  private List<Route> routes;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Route {
    private Summary summary;
    private List<Section> sections;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Summary {
    private int distance;
    private int duration;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Section {
    private List<Road> roads;
    private List<Guide> guides;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Road {
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
  public static class Guide {
    private String name;
    private int distance;
    private int duration;
    private String guidance;
    private Double x;
    private Double y;
    private int type;
  }
}
