package com.sparta.travelconquestbe.api.review.controller;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewCreateResponse> createReview(
      @Valid @RequestBody ReviewCreateRequest request,
      AuthUser authUser) {
    ReviewCreateResponse response = reviewService.createReview(request, authUser);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable Long reviewId,
      AuthUser authUser) {
    reviewService.deleteReview(reviewId, authUser);
    return ResponseEntity.noContent().build();
  }
}
