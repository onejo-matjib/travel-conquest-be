package com.sparta.travelconquestbe.api.bookmark.dto.response;

import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkCreateResponse {

  private Long routeId;
  private LocalDateTime createdAt;

  public static BookmarkCreateResponse from(Bookmark bookmark) {
    return BookmarkCreateResponse.builder()
        .routeId(bookmark.getRoute().getId())
        .createdAt(bookmark.getCreatedAt())
        .build();
  }
}
