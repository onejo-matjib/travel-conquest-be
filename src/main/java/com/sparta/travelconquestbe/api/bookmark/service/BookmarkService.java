package com.sparta.travelconquestbe.api.bookmark.service;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final RouteRepository routeRepository;

  @Transactional
  public BookmarkCreateResponse createBookmark(Long routeId, AuthUser authUser) {
    // 대상 루트가 존재하지 않을 경우 예외
    Route route = routeRepository.findById(routeId)
        .orElseThrow(
            () -> new CustomException("ROUTE_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    User user = User.builder().id(authUser.getUserId()).build();

    // 중복 여부 확인
    boolean isDuplicate = bookmarkRepository.isBookmarkExist(authUser.getUserId(), routeId);
    if (isDuplicate) {
      throw new CustomException("BOOKMARK_001", "이미 등록된 즐겨찾기입니다.", HttpStatus.CONFLICT);
    }

    Bookmark bookmark = Bookmark.createBookmark(user, route, false);

    Bookmark savedBookmark = bookmarkRepository.save(bookmark);
    return BookmarkCreateResponse.from(savedBookmark);
  }
}
