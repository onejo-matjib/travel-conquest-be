package com.sparta.travelconquestbe.api.review.service;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final RouteRepository routeRepository;

  //리뷰 등록
  @Transactional
  public ReviewCreateResponse createReview(ReviewCreateRequest request, AuthUser authUser) {
    // 루트가 존재하지 않을 경우 예외 처리
    Route route = routeRepository.findById(request.getRouteId())
        .orElseThrow(() ->
            new CustomException("ROUTE_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

    // 이미 해당 루트에 리뷰를 작성한 경우 예외 처리
    if (reviewRepository.isReviewExist(authUser.getUserId(), request.getRouteId())) {
      throw new CustomException("REVIEW_001", "이미 해당 루트에 리뷰를 작성했습니다.", HttpStatus.BAD_REQUEST);
    }

    // 리뷰 생성
    User user = new User(); // AuthUser에서 User 엔티티를 조회하는 로직 필요
    user.setId(authUser.getUserId());
    Review review = Review.createReview(request.getRating(), request.getComment(), route, user);
    Review savedReview = reviewRepository.save(review);

    return ReviewCreateResponse.from(savedReview);
  }

  // 리뷰 삭제
  @Transactional
  public void deleteReview(Long reviewId, AuthUser authUser) {
    // 리뷰 존재 여부 확인
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() ->
            new CustomException("REVIEW_002", "해당 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
    // 작성자 검증
    review.validateOwner(authUser.getUserId());
    // 리뷰 삭제
    reviewRepository.delete(review);
  }
}
