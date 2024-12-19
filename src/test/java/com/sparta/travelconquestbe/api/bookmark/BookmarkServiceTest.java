package com.sparta.travelconquestbe.api.bookmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
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

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BookmarkService bookmarkService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("즐겨찾기 등록 성공")
  void createBookmark_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long routeId = 2L;
    Route route = Route.builder().id(routeId).build();
    User mockUser = User.builder().id(user.getId()).build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(mockUser);
    when(bookmarkRepository.validateBookmarkCreation(user.getId(), routeId)).thenReturn("VALID");
    when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
    when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(
        Bookmark.builder().id(3L).user(mockUser).route(route).build()
    );

    BookmarkCreateResponse response = bookmarkService.createBookmark(routeId, user);

    assertNotNull(response);
    assertEquals(routeId, response.getRouteId());
    verify(bookmarkRepository).validateBookmarkCreation(user.getId(), routeId);
  }

  @Test
  @DisplayName("즐겨찾기 목록 조회 성공 - 페이징 검증")
  void searchBookmarks_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    int page = 1, size = 10;
    PageRequest pageRequest = PageRequest.of(page - 1, size);

    Page<BookmarkListResponse> mockPage = new PageImpl<>(Collections.singletonList(
        new BookmarkListResponse(1L, 2L, "Test Route", LocalDateTime.now())
    ));

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.getUserBookmarks(user.getId(), pageRequest)).thenReturn(mockPage);

    Page<BookmarkListResponse> result = bookmarkService.searchBookmarks(user, page, size);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(bookmarkRepository).getUserBookmarks(user.getId(), pageRequest);
  }

  @Test
  @DisplayName("즐겨찾기 등록 실패 - 루트 없음")
  void createBookmark_RouteNotFound() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long routeId = 2L;
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.validateBookmarkCreation(user.getId(), routeId)).thenReturn(
        "ROUTE_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, user);
    });

    assertEquals("BOOKMARK#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("즐겨찾기 등록 실패 - 중복 북마크")
  void createBookmark_DuplicateBookmark() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long routeId = 2L;

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.validateBookmarkCreation(user.getId(), routeId)).thenReturn(
        "DUPLICATE_BOOKMARK");

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(routeId, user);
    });

    assertEquals("BOOKMARK#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
  }

  @Test
  @DisplayName("즐겨찾기 삭제 성공")
  void deleteBookmark_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long bookmarkId = 3L;
    Bookmark bookmark = Bookmark.builder().id(bookmarkId)
        .user(User.builder().id(user.getId()).build()).build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

    bookmarkService.deleteBookmark(bookmarkId, user);

    verify(bookmarkRepository).delete(bookmark);
  }

  @Test
  @DisplayName("즐겨찾기 삭제 실패 - 북마크 없음")
  void deleteBookmark_NotFound() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long bookmarkId = 3L;

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(bookmarkId, user);
    });

    assertEquals("BOOKMARK#1_003", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("즐겨찾기 삭제 실패 - 본인 아님")
  void deleteBookmark_NotOwner() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long bookmarkId = 3L;
    Bookmark bookmark = Bookmark.builder().id(bookmarkId).user(User.builder().id(999L).build())
        .build();

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(bookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(bookmarkId, user);
    });

    assertEquals("BOOKMARK#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
  }
}
