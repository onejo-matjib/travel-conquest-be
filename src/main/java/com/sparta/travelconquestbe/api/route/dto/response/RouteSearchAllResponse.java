package com.sparta.travelconquestbe.api.route.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSearchAllResponse {
  private Long id;
  private String title;
  private String description;
  private String mediaUrl;
  private String creator;
  private Long locationCount;
  private Long reviewCount;
  private Long bookmarkCount;
  private LocalDateTime updatedAt;
}
