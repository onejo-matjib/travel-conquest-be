package com.sparta.travelconquestbe.api.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
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
import org.springframework.http.HttpStatus;

class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private RouteRepository routeRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ReviewService reviewService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("리뷰 등록 성공")
  void createReview_Success() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");

    User user = User.builder().id(userId).nickname("테스트 사용자").build();
    Route route = Route.builder().id(routeId).title("테스트 루트").build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.validateReviewCreation(userId, routeId)).thenReturn("VALID");
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review review = invocation.getArgument(0);
      return Review.builder()
          .id(1L)
          .rating(review.getRating())
          .comment(review.getComment())
          .route(review.getRoute())
          .user(review.getUser())
          .build();
    });

    ReviewCreateResponse response = reviewService.createReview(request, userId);

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    assertEquals(5, response.getRating());
    assertEquals("테스트 리뷰", response.getComment());
    assertEquals("테스트 사용자", response.getNickname());

    verify(userRepository, times(1)).findById(userId);
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).validateReviewCreation(userId, routeId);
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("리뷰 등록 실패 - 루트 없음")
  void createReview_RouteNotFound() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");

    when(reviewRepository.validateReviewCreation(userId, routeId)).thenReturn("ROUTE_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, userId);
    });

    assertEquals("REVIEW#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(reviewRepository, times(1)).validateReviewCreation(userId, routeId);
    verify(routeRepository, never()).findById(anyLong());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("리뷰 등록 실패 - 중복된 리뷰")
  void createReview_DuplicateReview() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");

    when(reviewRepository.validateReviewCreation(userId, routeId)).thenReturn("DUPLICATE_REVIEW");

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, userId);
    });

    assertEquals("REVIEW#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

    verify(reviewRepository, times(1)).validateReviewCreation(userId, routeId);
    verify(routeRepository, never()).findById(anyLong());
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("리뷰 삭제 성공")
  void deleteReview_Success() {
    Long reviewId = 1L;
    Long userId = 2L;

    User user = User.builder().id(userId).build();
    Review review = Review.builder().id(reviewId).user(user).build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    reviewService.deleteReview(reviewId, userId);

    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, times(1)).delete(review);
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 리뷰 없음")
  void deleteReview_ReviewNotFound() {
    Long reviewId = 1L;
    Long userId = 2L;

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, userId);
    });

    assertEquals("REVIEW#1_004", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());

    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, never()).delete(any());
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 권한 없음")
  void deleteReview_NotAuthorized() {
    Long reviewId = 1L;
    Long userId = 2L;
    Long otherUserId = 3L;

    User otherUser = User.builder().id(otherUserId).build();
    Review review = Review.builder().id(reviewId).user(otherUser).build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, userId);
    });

    assertEquals("REVIEW#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, never()).delete(any());
  }
}
