package com.sparta.travelconquestbe.api.review.service;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
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
  public ReviewCreateResponse createReview(ReviewCreateRequest request, AuthUserInfo user) {

    User referenceUser = userRepository.getReferenceById(user.getId());

    String validationResult = reviewRepository.validateReviewCreation(referenceUser.getId(),
        request.getRouteId());
    switch (validationResult) {
      case "ROUTE_NOT_FOUND":
        throw new CustomException("REVIEW#1_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      case "USER_NOT_FOUND":
        throw new CustomException("REVIEW#1_002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      case "DUPLICATE_REVIEW":
        throw new CustomException("REVIEW#2_001", "이미 해당 루트에 리뷰를 작성했습니다.", HttpStatus.BAD_REQUEST);
      default:
        break;
    }

    Route route = routeRepository.findById(request.getRouteId())
        .orElseThrow(
            () -> new CustomException("REVIEW#1_003", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    Review review = Review.createReview(request.getRating(), request.getComment(), route,
        referenceUser);
    Review savedReview = reviewRepository.save(review);

    return ReviewCreateResponse.from(savedReview);
  }

  @Transactional
  public void deleteReview(Long reviewId, AuthUserInfo user) {

    User referenceUser = userRepository.getReferenceById(user.getId());

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(
            () -> new CustomException("REVIEW#1_004", "해당 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    review.validateOwner(referenceUser.getId());
    reviewRepository.delete(review);
  }
}
