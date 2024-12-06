package com.sparta.travelconquestbe.api.review.controller;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  // 리뷰 등록 API
  @PostMapping
  public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request,
      AuthUser authUser) {
    ReviewResponse response = reviewService.createReview(request, authUser);
    return ResponseEntity.ok(response);
  }
}