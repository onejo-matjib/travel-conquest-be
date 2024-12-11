package com.sparta.travelconquestbe.api.bookmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

class BookmarkServiceTest {

  @Mock
  private BookmarkRepository bookmarkRepository;

  @Mock
  private RouteRepository routeRepository;

  @InjectMocks
  private BookmarkService bookmarkService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("즐겨찾기 등록 성공")
  void createBookmark_Success() {
    Long userId = 1L;
    Long routeId = 2L;

    Route route = Route.builder()
        .id(routeId)
        .title("Test Route")
        .build();

    when(bookmarkRepository.validateBookmarkCreation(userId, routeId)).thenReturn("VALID");
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(invocation -> {
      Bookmark bookmark = invocation.getArgument(0);
      return Bookmark.builder()
          .id(3L)
          .route(route)
          .user(bookmark.getUser())
          .build();
    });

    BookmarkCreateResponse response = bookmarkService.createBookmark(routeId, userId);

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    verify(bookmarkRepository, times(1)).validateBookmarkCreation(userId, routeId);
    verify(routeRepository, times(1)).findById(routeId);
    verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기 등록 실패 - 루트 존재하지 않음")
  void createBookmark_RouteNotFound() {
    Long userId = 1L;
    Long routeId = 2L;

    when(bookmarkRepository.validateBookmarkCreation(userId, routeId)).thenReturn(
        "ROUTE_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, userId);
    });

    assertEquals("BOOKMARK#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    verify(bookmarkRepository, times(1)).validateBookmarkCreation(userId, routeId);
    verify(routeRepository, never()).findById(routeId);
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기 등록 실패 - 중복")
  void createBookmark_DuplicateBookmark() {
    Long userId = 1L;
    Long routeId = 2L;

    when(bookmarkRepository.validateBookmarkCreation(userId, routeId)).thenReturn(
        "DUPLICATE_BOOKMARK");

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, userId);
    });

    assertEquals("BOOKMARK#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    verify(bookmarkRepository, times(1)).validateBookmarkCreation(userId, routeId);
    verify(routeRepository, never()).findById(routeId);
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기 목록 조회 성공")
  void getBookmarks_Success() {
    Long userId = 1L;

    BookmarkListResponse response = new BookmarkListResponse(1L, 2L, "Test Route", null);
    Page<BookmarkListResponse> page = new PageImpl<>(Collections.singletonList(response));

    when(bookmarkRepository.getUserBookmarks(eq(userId), any(PageRequest.class))).thenReturn(page);

    Page<BookmarkListResponse> result = bookmarkService.getBookmarks(userId, PageRequest.of(0, 10));

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(bookmarkRepository, times(1)).getUserBookmarks(eq(userId), any(PageRequest.class));
  }

  @Test
  @DisplayName("즐겨찾기 삭제 성공")
  void deleteBookmark_Success() {
    Long userId = 1L;
    Long bookmarkId = 3L;

    Bookmark bookmark = Bookmark.builder()
        .id(bookmarkId)
        .user(User.builder().id(userId).build())
        .build();

    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

    bookmarkService.deleteBookmark(bookmarkId, userId);

    verify(bookmarkRepository, times(1)).findById(bookmarkId);
    verify(bookmarkRepository, times(1)).delete(bookmark);
  }

  @Test
  @DisplayName("즐겨찾기 삭제 실패 - 찾을 수 없음")
  void deleteBookmark_NotFound() {
    Long userId = 1L;
    Long bookmarkId = 3L;

    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(bookmarkId, userId);
    });

    assertEquals("BOOKMARK#1_003", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    verify(bookmarkRepository, times(1)).findById(bookmarkId);
    verify(bookmarkRepository, never()).delete(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기 삭제 실패 - 본인 아님")
  void deleteBookmark_NotOwner() {
    Long userId = 1L;
    Long bookmarkId = 3L;

    Bookmark bookmark = Bookmark.builder()
        .id(bookmarkId)
        .user(User.builder().id(2L).build()) // 다른 유저 ID
        .build();

    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(bookmarkId, userId);
    });

    assertEquals("BOOKMARK#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    verify(bookmarkRepository, times(1)).findById(bookmarkId);
    verify(bookmarkRepository, never()).delete(any(Bookmark.class));
  }
}
