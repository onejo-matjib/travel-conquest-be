package com.sparta.travelconquestbe.api.review.dto.respones;

import com.sparta.travelconquestbe.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {

  private Long reviewId;
  private int rating;
  private String comment;
  private Long routeId;

  // 정적 팩토리 메서드 추가
  public static ReviewResponse from(Review review) {
    return ReviewResponse.builder()
        .reviewId(review.getId())
        .rating(review.getRating())
        .comment(review.getComment())
        .routeId(review.getRoute().getId())
        .build();
  }
}
