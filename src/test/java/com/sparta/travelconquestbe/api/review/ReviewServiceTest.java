package com.sparta.travelconquestbe.api.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private RouteRepository routeRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ReviewService reviewService;

  private Long userId;
  private Long routeId;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userId = 1L;
    routeId = 10L;
  }

  @Test
  @DisplayName("리뷰 생성 성공")
  void createReviewSuccess() {
    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "좋은 루트입니다!");

    User user = User.builder().id(userId).nickname("Tester").build();
    Route route = Route.builder().id(routeId).title("Test Route").build();
    Review review = Review.builder().id(100L).rating(5).comment("좋은 루트입니다!").user(user).route(route)
        .build();

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.validateReviewCreation(userId, routeId)).thenReturn("VALID");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(reviewRepository.save(any(Review.class))).thenReturn(review);

    ReviewCreateResponse response = reviewService.createReview(request, userId);

    assertNotNull(response);
    assertEquals(5, response.getRating());
    assertEquals("좋은 루트입니다!", response.getComment());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("리뷰 생성 실패 - 이미 존재하는 리뷰")
  void createReviewFailure_DuplicateReview() {
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(new Route()));
    when(reviewRepository.validateReviewCreation(userId, routeId)).thenReturn("DUPLICATE_REVIEW");

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "좋은 루트입니다!");

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.createReview(request, userId));

    assertEquals("REVIEW_001", exception.getErrorCode());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("리뷰 삭제 성공")
  void deleteReviewSuccess() {
    Review review = Review.builder().id(100L).user(User.builder().id(userId).build()).build();

    when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

    reviewService.deleteReview(100L, userId);

    verify(reviewRepository, times(1)).delete(review);
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 본인이 아님")
  void deleteReviewFailure_NotOwner() {
    Review review = Review.builder().id(100L).user(User.builder().id(2L).build()).build();

    when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.deleteReview(100L, userId));

    assertEquals("REVIEW_003", exception.getErrorCode());
    verify(reviewRepository, never()).delete(any());
  }
}
