package com.sparta.travelconquestbe.api.bookmark.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BookmarkListResponse {

  private Long bookmarkId;
  private Long routeId;
  private String routeTitle;
  private LocalDateTime createdAt;

  public BookmarkListResponse(Long bookmarkId, Long routeId, String routeTitle,
      LocalDateTime createdAt) {
    this.bookmarkId = bookmarkId;
    this.routeId = routeId;
    this.routeTitle = routeTitle;
    this.createdAt = createdAt;
  }
}
