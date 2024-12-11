package com.sparta.travelconquestbe.api.review.service;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final RouteRepository routeRepository;

  @Transactional
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    String validationResult = reviewRepository.validateReviewCreation(userId, request.getRouteId());
    switch (validationResult) {
      case "ROUTE_NOT_FOUND":
        throw new CustomException("ROUTE#1_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      case "DUPLICATE_REVIEW":
        throw new CustomException("REVIEW#1_001", "이미 해당 루트에 리뷰를 작성했습니다.", HttpStatus.BAD_REQUEST);
      default:
        break;
    }

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("USER#1_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    Route route = routeRepository.findById(request.getRouteId())
        .orElseThrow(
            () -> new CustomException("ROUTE#1_002", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    Review review = Review.createReview(
        request.getRating(),
        request.getComment(),
        route,
        user
    );
    return ReviewCreateResponse.from(reviewRepository.save(review));
  }

  @Transactional
  public void deleteReview(Long id, Long userId) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(
            () -> new CustomException("REVIEW#2_001", "해당 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    review.validateOwner(userId);
    reviewRepository.delete(review);
  }
}
