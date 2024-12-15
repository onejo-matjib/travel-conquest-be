package com.sparta.travelconquestbe.api.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewCreateRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewCreateResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
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
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");
    User mockUser = User.builder().id(user.getId()).nickname("테스트 사용자").build();
    Route route = Route.builder().id(routeId).title("테스트 루트").build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(mockUser);
    when(reviewRepository.validateReviewCreation(user.getId(), routeId)).thenReturn("VALID");
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
      Review r = inv.getArgument(0);
      return Review.builder()
          .id(1L)
          .rating(r.getRating())
          .comment(r.getComment())
          .route(r.getRoute())
          .user(r.getUser())
          .build();
    });

    ReviewCreateResponse response = reviewService.createReview(request, user);

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    assertEquals(5, response.getRating());
    assertEquals("테스트 리뷰", response.getComment());
    assertEquals("테스트 사용자", response.getNickname());
  }

  @Test
  @DisplayName("리뷰 등록 실패 - 루트 없음")
  void createReview_RouteNotFound() {
    Long routeId = 1L;
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(reviewRepository.validateReviewCreation(user.getId(), routeId)).thenReturn(
        "ROUTE_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, user);
    });

    assertEquals("REVIEW#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("리뷰 등록 실패 - 중복 리뷰")
  void createReview_DuplicateReview() {
    Long routeId = 1L;
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "테스트 리뷰");

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(reviewRepository.validateReviewCreation(user.getId(), routeId)).thenReturn(
        "DUPLICATE_REVIEW");

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, user);
    });

    assertEquals("REVIEW#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }

  @Test
  @DisplayName("리뷰 삭제 성공")
  void deleteReview_Success() {
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long reviewId = 1L;
    Review review = Review.builder().id(reviewId).user(User.builder().id(user.getId()).build())
        .build();
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    reviewService.deleteReview(reviewId, user);

    verify(reviewRepository).delete(review);
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 리뷰 없음")
  void deleteReview_ReviewNotFound() {
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long reviewId = 1L;
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, user);
    });

    assertEquals("REVIEW#1_004", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 권한 없음")
  void deleteReview_NotAuthorized() {
    AuthUserInfo user = new AuthUserInfo(2L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long reviewId = 1L;
    Review review = Review.builder().id(reviewId).user(User.builder().id(999L).build()).build();
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, user);
    });

    assertEquals("REVIEW#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
  }
}
