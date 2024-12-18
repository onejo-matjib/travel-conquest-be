package com.sparta.travelconquestbe.api.review.controller;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewCreateResponse> createReview(
      @Valid @RequestBody ReviewCreateRequest request,
      @AuthUser AuthUserInfo user
  ) {
    ReviewCreateResponse response = reviewService.createReview(request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable Long id,
      @AuthUser AuthUserInfo user
  ) {
    reviewService.deleteReview(id, user);
    return ResponseEntity.noContent().build();
  }
}
