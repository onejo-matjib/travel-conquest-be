package com.sparta.travelconquestbe.api.bookmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
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

class BookmarkServiceTest {

  @Mock
  private BookmarkRepository bookmarkRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RouteRepository routeRepository;

  @InjectMocks
  private BookmarkService bookmarkService;

  private Long userId;
  private Long routeId;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userId = 1L;
    routeId = 10L;
  }

  @Test
  @DisplayName("즐겨찾기 생성 성공")
  void createBookmarkSuccess() {
    // Mock Data
    Route route = Route.builder()
        .id(routeId)
        .title("Test Route")
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("Tester")
        .build();

    Bookmark bookmark = Bookmark.builder()
        .id(1L)
        .route(route) // Route 초기화
        .user(user)   // User 초기화
        .build();

    // Mock 설정
    when(bookmarkRepository.validateBookmarkCreation(userId, routeId)).thenReturn("VALID");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(bookmarkRepository.save(any())).thenReturn(bookmark);

    // Service 호출
    BookmarkCreateResponse response = bookmarkService.createBookmark(routeId, userId);

    // 검증
    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    verify(bookmarkRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("즐겨찾기 생성 실패 - 중복")
  void createBookmarkFailure_Duplicate() {
    when(bookmarkRepository.validateBookmarkCreation(userId, routeId)).thenReturn(
        "DUPLICATE_BOOKMARK");

    CustomException exception = assertThrows(CustomException.class,
        () -> bookmarkService.createBookmark(routeId, userId));

    assertEquals("BOOKMARK_001", exception.getErrorCode());
    verify(bookmarkRepository, never()).save(any());
  }

  @Test
  @DisplayName("즐겨찾기 삭제 성공")
  void deleteBookmarkSuccess() {
    Bookmark bookmark = Bookmark.builder().user(User.builder().id(userId).build()).build();

    when(bookmarkRepository.findById(100L)).thenReturn(Optional.of(bookmark));

    bookmarkService.deleteBookmark(100L, userId);

    verify(bookmarkRepository, times(1)).delete(bookmark);
  }

  @Test
  @DisplayName("즐겨찾기 삭제 실패 - 본인이 아님")
  void deleteBookmarkFailure_NotOwner() {
    Bookmark bookmark = Bookmark.builder().user(User.builder().id(2L).build()).build();

    when(bookmarkRepository.findById(100L)).thenReturn(Optional.of(bookmark));

    CustomException exception = assertThrows(CustomException.class,
        () -> bookmarkService.deleteBookmark(100L, userId));

    assertEquals("BOOKMARK_003", exception.getErrorCode());
    verify(bookmarkRepository, never()).delete(any());
  }
}
