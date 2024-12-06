package com.sparta.travelconquestbe.api.review.dto.respones;

import com.sparta.travelconquestbe.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateResponse {

  private Long reviewId;
  private int rating;
  private String comment;
  private Long routeId;
  private String nickname;

  public static ReviewCreateResponse from(Review review) {
    return ReviewCreateResponse.builder()
        .reviewId(review.getId())
        .rating(review.getRating())
        .comment(review.getComment())
        .routeId(review.getRoute().getId())
        .nickname(review.getUser().getNickname())
        .build();
  }
}
