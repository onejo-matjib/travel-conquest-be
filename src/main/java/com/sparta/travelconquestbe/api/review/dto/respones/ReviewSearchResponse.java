package com.sparta.travelconquestbe.api.review.dto.respones;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewSearchResponse {
  private int rating;
  private String comment;
  private String nickname;

  @Builder
  public ReviewSearchResponse(int rating, String comment, String nickname) {
    this.rating = rating;
    this.comment = comment;
    this.nickname = nickname;
  }
}
