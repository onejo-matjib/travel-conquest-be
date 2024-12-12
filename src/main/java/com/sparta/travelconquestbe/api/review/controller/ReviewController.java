package com.sparta.travelconquestbe.api.review.controller;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewCreateResponse> createReview(
      @Valid @RequestBody ReviewCreateRequest request,
      @AuthUser Long userId) {
    ReviewCreateResponse response = reviewService.createReview(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable Long id,
      @AuthUser Long userId) {
    reviewService.deleteReview(id, userId);
    return ResponseEntity.noContent().build();
  }
}
