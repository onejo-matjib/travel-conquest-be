package com.sparta.travelconquestbe.api.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
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
    // Given
    Long routeId = 1L;
    Long userId = 10L;

    ReviewRequest request = new ReviewRequest(routeId, 5, "루트가 맘에들어요!");
    AuthUser authUser = new AuthUser(userId);

    Route route = Route.builder()
        .id(routeId)
        .title("테스트 루트")
        .description("테스트 루트입니다")
        .build();

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.existsByUserIdAndRouteId(userId, routeId)).thenReturn(false);
    when(reviewRepository.save(any(Review.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    // When
    ReviewResponse response = reviewService.createReview(request, authUser);

    // Then
    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    assertEquals(5, response.getRating());
    assertEquals("루트가 맘에들어요!", response.getComment());

    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).existsByUserIdAndRouteId(userId, routeId);
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  void createReview_RouteNotFound() {
    // Given
    Long routeId = 1L;
    Long userId = 10L;

    ReviewRequest request = new ReviewRequest(routeId, 5, "루트가 맘에들어요!");
    AuthUser authUser = new AuthUser(userId);

    when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser);
    });

    assertEquals("ROUTE_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void createReview_ReviewAlreadyExists() {
    // Given
    Long routeId = 1L;
    Long userId = 10L;

    ReviewRequest request = new ReviewRequest(routeId, 5, "루트가 맘에들어요!");
    AuthUser authUser = new AuthUser(userId);

    Route route = Route.builder()
        .id(routeId)
        .title("테스트 루트")
        .description("테스트 루트입니다")
        .build();

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(reviewRepository.existsByUserIdAndRouteId(userId, routeId)).thenReturn(true);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () -> {
      reviewService.createReview(request, authUser);
    });

    assertEquals("REVIEW_001", exception.getErrorCode());
    verify(routeRepository, times(1)).findById(routeId);
    verify(reviewRepository, times(1)).existsByUserIdAndRouteId(userId, routeId);
    verify(reviewRepository, never()).save(any(Review.class));
  }
}
