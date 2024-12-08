package com.sparta.travelconquestbe.api.bookmark.dto.response;

import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkListResponse {

  private Long bookmarkId;
  private Long routeId;
  private String routeTitle;
  private LocalDateTime createdAt;

  public static BookmarkListResponse from(Bookmark bookmark) {
    return BookmarkListResponse.builder()
        .bookmarkId(bookmark.getId())
        .routeId(bookmark.getRoute().getId())
        .routeTitle(bookmark.getRoute().getTitle())
        .createdAt(bookmark.getCreatedAt())
        .build();
  }
}
