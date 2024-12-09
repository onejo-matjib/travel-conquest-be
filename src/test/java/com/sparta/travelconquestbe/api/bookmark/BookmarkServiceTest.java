package com.sparta.travelconquestbe.api.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
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
import java.util.List;
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

  @InjectMocks
  private BookmarkService bookmarkService;

  @Mock
  private BookmarkRepository bookmarkRepository;

  @Mock
  private RouteRepository routeRepository;

  private User mockUser;
  private Route mockRoute;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockUser = User.builder().id(1L).build();
    mockRoute = Route.builder().id(1L).title("Test Route").build();
  }

  @Test
  @DisplayName("즐겨찾기_등록_성공")
  void testCreateBookmark_Success() {
    when(routeRepository.findById(mockRoute.getId())).thenReturn(Optional.of(mockRoute));
    when(bookmarkRepository.isBookmarkExist(mockUser.getId(), mockRoute.getId())).thenReturn(false);
    when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    BookmarkCreateResponse response = bookmarkService.createBookmark(mockRoute.getId(),
        mockUser.getId());

    assertThat(response.getRouteId()).isEqualTo(mockRoute.getId());

    verify(routeRepository, times(1)).findById(mockRoute.getId());
    verify(bookmarkRepository, times(1)).isBookmarkExist(mockUser.getId(), mockRoute.getId());
    verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기_등록_실패_루트_존재하지_않음")
  void testCreateBookmark_Fail_RouteNotFound() {
    when(routeRepository.findById(mockRoute.getId())).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(mockRoute.getId(), mockUser.getId());
    });

    assertThat(exception.getErrorCode()).isEqualTo("ROUTE_001");
    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    verify(routeRepository, times(1)).findById(mockRoute.getId());
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("이미_등록된_즐겨찾기")
  void testCreateBookmark_Fail_DuplicateBookmark() {
    when(routeRepository.findById(mockRoute.getId())).thenReturn(Optional.of(mockRoute));
    when(bookmarkRepository.isBookmarkExist(mockUser.getId(), mockRoute.getId())).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(mockRoute.getId(), mockUser.getId());
    });

    assertThat(exception.getErrorCode()).isEqualTo("BOOKMARK_001");
    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

    verify(routeRepository, times(1)).findById(mockRoute.getId());
    verify(bookmarkRepository, times(1)).isBookmarkExist(mockUser.getId(), mockRoute.getId());
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기_목록_조회_성공")
  void testGetBookmarks_Success() {
    BookmarkListResponse mockResponse = new BookmarkListResponse(
        1L, mockRoute.getId(), mockRoute.getTitle(), mockRoute.getCreatedAt()
    );

    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<BookmarkListResponse> mockPage = new PageImpl<>(List.of(mockResponse), pageRequest, 1);

    when(bookmarkRepository.getUserBookmarks(mockUser.getId(), pageRequest)).thenReturn(mockPage);

    Page<BookmarkListResponse> response = bookmarkService.getBookmarks(mockUser.getId(),
        pageRequest);

    assertThat(response.getContent().size()).isEqualTo(1);
    assertThat(response.getContent().get(0).getBookmarkId()).isEqualTo(
        mockResponse.getBookmarkId());
    assertThat(response.getContent().get(0).getRouteId()).isEqualTo(mockResponse.getRouteId());

    verify(bookmarkRepository, times(1)).getUserBookmarks(mockUser.getId(), pageRequest);
  }

  @Test
  @DisplayName("즐겨찾기_목록_조회_빈_결과")
  void testGetBookmarks_EmptyResult() {
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<BookmarkListResponse> mockPage = new PageImpl<>(List.of(), pageRequest, 0);

    when(bookmarkRepository.getUserBookmarks(mockUser.getId(), pageRequest)).thenReturn(mockPage);

    Page<BookmarkListResponse> response = bookmarkService.getBookmarks(mockUser.getId(),
        pageRequest);

    assertThat(response.getContent().size()).isEqualTo(0);

    verify(bookmarkRepository, times(1)).getUserBookmarks(mockUser.getId(), pageRequest);
  }

  @Test
  @DisplayName("즐겨찾기_삭제_성공")
  void testDeleteBookmark_Success() {
    Bookmark mockBookmark = Bookmark.builder()
        .id(1L)
        .user(mockUser)
        .build();

    when(bookmarkRepository.findById(mockBookmark.getId())).thenReturn(Optional.of(mockBookmark));

    bookmarkService.deleteBookmark(mockBookmark.getId(), mockUser.getId());

    verify(bookmarkRepository, times(1)).findById(mockBookmark.getId());
    verify(bookmarkRepository, times(1)).delete(mockBookmark);
  }

  @Test
  @DisplayName("즐겨찾기_삭제_실패_존재하지_않음")
  void testDeleteBookmark_Fail_NotFound() {
    when(bookmarkRepository.findById(1L)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(1L, mockUser.getId());
    });

    assertThat(exception.getErrorCode()).isEqualTo("BOOKMARK_002");
    verify(bookmarkRepository, times(1)).findById(1L);
    verify(bookmarkRepository, never()).delete(any());
  }

  @Test
  @DisplayName("즐겨찾기_삭제_실패_본인아님")
  void testDeleteBookmark_Fail_NotOwner() {
    User anotherUser = User.builder().id(2L).build();
    Bookmark anotherBookmark = Bookmark.builder()
        .id(1L)
        .user(anotherUser)
        .build();

    when(bookmarkRepository.findById(anotherBookmark.getId())).thenReturn(
        Optional.of(anotherBookmark));

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.deleteBookmark(anotherBookmark.getId(), mockUser.getId());
    });

    assertThat(exception.getErrorCode()).isEqualTo("BOOKMARK_003");
    verify(bookmarkRepository, times(1)).findById(anotherBookmark.getId());
    verify(bookmarkRepository, never()).delete(any());
  }
}
