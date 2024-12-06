package com.sparta.travelconquestbe.api.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.travelconquestbe.api.review.dto.request.ReviewRequest;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewResponse;
import com.sparta.travelconquestbe.api.review.service.ReviewService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.review.repository.ReviewRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;

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

	@Test
	void createReview_CannotReviewOwnRoute() { // 유저 DB 임의로 넣고 테스트 통과
		// Given
		Long routeId = 1L;
		Long userId = 10L; // 리뷰 작성자와 루트 소유자 동일

		ReviewRequest request = new ReviewRequest(routeId, 5, "Great route!");
		AuthUser authUser = new AuthUser(userId);

		Route route = Route.builder()
			.id(routeId)
			.title("Test Route")
			.description("This is a test route")
			.user(User.builder().id(userId).build()) // 루트 소유자 ID와 리뷰 작성자 ID 동일
			.build();

		when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> {
			reviewService.createReview(request, authUser);
		});

		assertEquals("REVIEW_002", exception.getErrorCode());
		verify(routeRepository, times(1)).findById(routeId);
		verify(reviewRepository, never()).save(any(Review.class));
	}
}
