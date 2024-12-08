package com.sparta.travelconquestbe.api.bookmark.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkCreateRequest {

  @NotNull(message = "Route ID는 필수입니다.")
  private Long routeId;

  public BookmarkCreateRequest(Long routeId) {
    this.routeId = routeId;
  }
}
