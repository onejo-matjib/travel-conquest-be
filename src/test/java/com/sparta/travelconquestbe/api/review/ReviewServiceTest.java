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
import com.sparta.travelconquestbe.common.auth.AuthUser;
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
  @DisplayName("리뷰_등록_성공")
  void createReview_Success() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "루트가 맘에들어요!");
    AuthUser authUser = new AuthUser(userId);

    User routeOwner = User.builder()
        .id(20L)
        .nickname("루트 유저 닉네임")
        .build();

    User reviewer = User.builder()
        .id(userId)
        .nickname("리뷰 유저 닉네임")
        .build();

    Route route = Route.builder()
        .id(routeId)
        .title("테스트 루트")
        .description("테스트 루트입니다")
        .user(routeOwner)
        .build();

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.isReviewExist(userId, routeId)).thenReturn(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(reviewer));

    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review review = invocation.getArgument(0);
      return Review.builder()
          .id(1L)
          .rating(review.getRating())
          .comment(review.getComment())
          .route(review.getRoute())
          .user(reviewer)
          .build();
    });

    ReviewCreateResponse response = reviewService.createReview(request, authUser.getUserId());

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    assertEquals(5, response.getRating());
    assertEquals("루트가 맘에들어요!", response.getComment());
    assertEquals("리뷰 유저 닉네임", response.getNickname());

    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).isReviewExist(userId, routeId);
    verify(userRepository, times(1)).findById(userId);
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("리뷰_등록_실패_루트_존재하지_않음")
  void createReview_RouteNotFound() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "루트가 너무 좋아요!");
    AuthUser authUser = new AuthUser(userId);

    when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser.getUserId());
    });

    assertEquals("ROUTE#1_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, never()).save(any(Review.class));
    verify(userRepository, never()).findById(anyLong());
  }

  @Test
  @DisplayName("이미_등록된_리뷰_있음")
  void createReview_ReviewAlreadyExists() {
    Long routeId = 1L;
    Long userId = 10L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "루트가 맘에들어요!");
    AuthUser authUser = new AuthUser(userId);

    User owner = User.builder()
        .id(20L)
        .nickname("RouteOwnerNick")
        .build();

    Route route = Route.builder()
        .id(routeId)
        .title("테스트 루트")
        .description("테스트 루트입니다")
        .user(owner)
        .build();

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.isReviewExist(userId, routeId)).thenReturn(true);
    // 리뷰 중복 시 user 조회 안함 -> 필요 없음

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser.getUserId());
    });

    assertEquals("REVIEW_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).isReviewExist(userId, routeId);
    verify(userRepository, never()).findById(anyLong());
    verify(reviewRepository, never()).save(any(Review.class));
  }

  // 리뷰 삭제
  @Test
  @DisplayName("리뷰_삭제_성공")
  void deleteReview_Success() {
    Long reviewId = 1L;
    Long userId = 2L;

    AuthUser authUser = new AuthUser(userId);

    User reviewer = User.builder()
        .id(userId)
        .nickname("리뷰 유저 닉네임")
        .build();

    Review review = Review.builder()
        .id(reviewId)
        .comment("리뷰 내용")
        .user(reviewer)
        .build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    reviewService.deleteReview(reviewId, authUser.getUserId());

    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, times(1)).delete(review);
  }

  @Test
  @DisplayName("리뷰_삭제_실패_리뷰_찾을수_없음")
  void deleteReview_ReviewNotFound() {
    Long reviewId = 1L;
    Long userId = 2L;

    AuthUser authUser = new AuthUser(userId);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, authUser.getUserId());
    });

    assertEquals("REVIEW_002", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, never()).delete(any());
  }

  @Test
  @DisplayName("리뷰_삭제_실패_리뷰_주인_아님")
  void deleteReview_NotOwner() {
    Long reviewId = 1L;
    Long userId = 2L;      // 실제 삭제하려는 유저
    Long otherUserId = 3L; // 리뷰 작성자와 다른 유저

    AuthUser authUser = new AuthUser(userId);

    User otherUser = User.builder()
        .id(otherUserId)
        .nickname("다른 유저 닉네임")
        .build();

    Review review = Review.builder()
        .id(reviewId)
        .comment("리뷰 내용")
        .user(otherUser) // 작성자가 다른 유저
        .build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.deleteReview(reviewId, authUser.getUserId());
    });

    assertEquals("REVIEW_003", exception.getErrorCode());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    verify(reviewRepository, times(1)).findById(reviewId);
    verify(reviewRepository, never()).delete(any());
  }
}
