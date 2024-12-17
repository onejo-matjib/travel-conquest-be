package com.sparta.travelconquestbe.api.bookmark.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BookmarkRankingResponse {

  private String creatorName;
  private String title;
  private String description;
  private LocalDateTime updatedAt;

  public BookmarkRankingResponse(String creatorName, String title, String description,
      LocalDateTime updatedAt, LocalDateTime createdAt) {
    this.updatedAt = updatedAt != null ? updatedAt : createdAt; //updatedAt이 NULL이면 createdAt 사용
    this.creatorName = creatorName;
    this.title = title;
    this.description = description;
  }
}
