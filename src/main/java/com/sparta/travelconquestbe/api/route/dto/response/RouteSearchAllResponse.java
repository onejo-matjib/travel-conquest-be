package com.sparta.travelconquestbe.api.route.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class RouteSearchAllResponse {
  private Long id;
  private String title;
  private String description;
  private String mediaUrl;
  private String creator;
  private Long locationCount;
  private Long reviewCount;
  private Long bookmarkCount;
  private Double reviewAvg;
  private LocalDateTime updatedAt;

  public RouteSearchAllResponse(
      Long id,
      String title,
      String description,
      String mediaUrl,
      String creator,
      Long locationCount,
      Long reviewCount,
      Long bookmarkCount,
      Double reviewAvg,
      LocalDateTime updatedAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.mediaUrl = mediaUrl;
    this.creator = creator;
    this.locationCount = locationCount;
    this.reviewCount = reviewCount;
    this.bookmarkCount = bookmarkCount;
    this.reviewAvg = reviewAvg;
    this.updatedAt = updatedAt;
  }
}
