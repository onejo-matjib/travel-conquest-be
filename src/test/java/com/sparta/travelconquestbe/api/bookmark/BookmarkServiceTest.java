package com.sparta.travelconquestbe.api.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

class BookmarkServiceTest {

  @InjectMocks
  private BookmarkService bookmarkService;

  @Mock
  private BookmarkRepository bookmarkRepository;

  @Mock
  private RouteRepository routeRepository;

  private AuthUser authUser;
  private Route mockRoute;
  private User mockUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    authUser = new AuthUser(1L); // Mock User ID
    mockRoute = Route.builder().id(1L).title("Test Route").build();
    mockUser = User.builder().id(authUser.getUserId()).build();
  }

  @Test
  @DisplayName("즐겨찾기_등록_성공")
  void testCreateBookmark_Success() {
    when(routeRepository.findById(mockRoute.getId())).thenReturn(Optional.of(mockRoute));
    when(bookmarkRepository.isBookmarkExist(authUser.getUserId(), mockRoute.getId()))
        .thenReturn(false);
    when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    BookmarkCreateResponse response = bookmarkService.createBookmark(mockRoute.getId(), authUser);

    assertThat(response.getRouteId()).isEqualTo(mockRoute.getId());

    verify(routeRepository, times(1)).findById(mockRoute.getId());
    verify(bookmarkRepository, times(1)).isBookmarkExist(authUser.getUserId(), mockRoute.getId());
    verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("즐겨찾기_등록_실패_루트_존재하지_않음")
  void testCreateBookmark_Fail_RouteNotFound() {
    when(routeRepository.findById(mockRoute.getId())).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(mockRoute.getId(), authUser);
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
    when(bookmarkRepository.isBookmarkExist(authUser.getUserId(), mockRoute.getId()))
        .thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      bookmarkService.createBookmark(mockRoute.getId(), authUser);
    });

    assertThat(exception.getErrorCode()).isEqualTo("BOOKMARK_001");
    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

    verify(routeRepository, times(1)).findById(mockRoute.getId());
    verify(bookmarkRepository, times(1)).isBookmarkExist(authUser.getUserId(), mockRoute.getId());
    verify(bookmarkRepository, never()).save(any(Bookmark.class));
  }
}
