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
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private RouteRepository routeRepository;

  @InjectMocks
  private ReviewService reviewService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
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
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review review = invocation.getArgument(0);
      return Review.builder()
          .id(1L)
          .rating(review.getRating())
          .comment(review.getComment())
          .route(review.getRoute())
          .user(reviewer) // user 설정
          .build();
    });

    ReviewCreateResponse response = reviewService.createReview(request, authUser);

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    assertEquals(5, response.getRating());
    assertEquals("루트가 맘에들어요!", response.getComment());
    assertEquals("리뷰 유저 닉네임", response.getNickname());

    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).isReviewExist(userId, routeId);
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  void createReview_RouteNotFound() {
    Long routeId = 1L;
    Long userId = 2L;

    ReviewCreateRequest request = new ReviewCreateRequest(routeId, 5, "루트가 너무 좋아요!");
    AuthUser authUser = new AuthUser(userId);

    when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser);
    });

    assertEquals("ROUTE_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
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

    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser);
    });

    assertEquals("REVIEW_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).isReviewExist(userId, routeId);
    verify(reviewRepository, never()).save(any(Review.class));
  }
}
