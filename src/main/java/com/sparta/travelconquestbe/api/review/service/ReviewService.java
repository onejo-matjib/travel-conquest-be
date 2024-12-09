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
  private final RouteRepository routeRepository;
  private final UserRepository userRepository;

  @Transactional
  public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
    Route route = routeRepository.findById(request.getRouteId())
        .orElseThrow(
            () -> new CustomException("ROUTE_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (reviewRepository.isReviewExist(userId, request.getRouteId())) {
      throw new CustomException("REVIEW_001", "이미 해당 루트에 리뷰를 작성했습니다.", HttpStatus.BAD_REQUEST);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    Review review = Review.createReview(request.getRating(), request.getComment(), route, user);
    Review savedReview = reviewRepository.save(review);

    return ReviewCreateResponse.from(savedReview);
  }

  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(
            () -> new CustomException("REVIEW_002", "해당 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (!review.getUser().getId().equals(userId)) {
      throw new CustomException("REVIEW_003", "본인의 리뷰만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
    }

    reviewRepository.delete(review);
  }
}
