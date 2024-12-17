package com.sparta.travelconquestbe.api.route.dto.response;

import com.sparta.travelconquestbe.api.review.dto.respones.ReviewSearchResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.respones.LocationSearchResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RouteSearchResponse {
  private String title;
  private String description;
  private Long totalDistance;
  private int money;
  private String estimatedTime;
  private String creator;

  private List<LocationSearchResponse> routeLocations;
  private RouteLineResponse routeLine;
  private List<ReviewSearchResponse> reviews;

  @Builder
  public RouteSearchResponse(
      String title,
      String description,
      Long totalDistance,
      int money,
      String estimatedTime,
      String creator,
      List<LocationSearchResponse> routeLocations,
      RouteLineResponse routeLine,
      List<ReviewSearchResponse> reviews) {
    this.title = title;
    this.description = description;
    this.totalDistance = totalDistance;
    this.money = money;
    this.estimatedTime = estimatedTime;
    this.creator = creator;
    this.routeLocations = routeLocations;
    this.routeLine = routeLine;
    this.reviews = reviews;
  }
}
