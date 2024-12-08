package com.sparta.travelconquestbe.api.bookmark.service;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BookmarkServiceTest {

  @InjectMocks
  private BookmarkService bookmarkService;

  @Mock
  private BookmarkRepository bookmarkRepository;

  @Mock
  private RouteRepository routeRepository;

  private AuthUser authUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    authUser = new AuthUser(1L); // Mock User ID
  }

  @Test
  @DisplayName("즐겨찾기_등록_성공")
  void testCreateBookmark_Success() {
    Long routeId = 1L;
    Route mockRoute = Route.builder().id(routeId).title("Test Route").build();
    User mockUser = User.builder().id(authUser.getUserId()).build();
    Bookmark mockBookmark = Bookmark.createBookmark(mockUser, mockRoute);

    when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));
    when(bookmarkRepository.isBookmarkExist(authUser.getUserId(), routeId)).thenReturn(false);
    when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(mockBookmark);

    BookmarkCreateResponse response = bookmarkService.createBookmark(routeId, authUser);

    assertThat(response.getRouteId()).isEqualTo(routeId);
    verify(routeRepository, times(1)).findById(routeId);
    verify(bookmarkRepository, times(1)).isBookmarkExist(authUser.getUserId(), routeId);
    verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기_등록_실패_루트_존재하지_않음")
  void testCreateBookmark_Fail_RouteNotFound() {
    Long routeId = 999L;
    when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, authUser);
    });

    assertThat(exception.getErrorCode()).isEqualTo("ROUTE_001");
    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(routeRepository, times(1)).findById(routeId);
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("이미_등록된_즐겨찾기")
  void testCreateBookmark_Fail_DuplicateBookmark() {
    Long routeId = 1L;
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(new Route()));
    when(bookmarkRepository.isBookmarkExist(authUser.getUserId(), routeId)).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, authUser);
    });

    assertThat(exception.getErrorCode()).isEqualTo("BOOKMARK_001");
    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    verify(routeRepository, times(1)).findById(routeId);
    verify(bookmarkRepository, times(1)).isBookmarkExist(authUser.getUserId(), routeId);
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }
}
