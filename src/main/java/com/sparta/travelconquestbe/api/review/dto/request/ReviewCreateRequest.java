package com.sparta.travelconquestbe.api.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewCreateRequest {

  @NotNull(message = "Route Id는 필수입니다")
  private Long routeId;

  @Min(value = 1, message = "최소 평점은 1점 입니다.")
  @Max(value = 5, message = "최대 평점은 5점 입니다.")
  private int rating;

  @NotBlank(message = "내용을 입력해주세요.")
  private String comment;

  public ReviewCreateRequest(Long routeId, int rating, String comment) {
    this.routeId = routeId;
    this.rating = rating;
    this.comment = comment;
  }
}
