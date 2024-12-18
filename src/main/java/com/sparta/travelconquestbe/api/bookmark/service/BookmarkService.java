package com.sparta.travelconquestbe.api.bookmark.service;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkRankingResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.bookmark.repository.BookmarkRepository;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final RouteRepository routeRepository;
  private final UserRepository userRepository;

  @Transactional
  public BookmarkCreateResponse createBookmark(Long routeId, AuthUserInfo user) {
    User referenceUser = userRepository.getReferenceById(user.getId());

    String validationResult = bookmarkRepository.validateBookmarkCreation(referenceUser.getId(),
        routeId);
    switch (validationResult) {
      case "ROUTE_NOT_FOUND":
        throw new CustomException("BOOKMARK#1_001", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      case "DUPLICATE_BOOKMARK":
        throw new CustomException("BOOKMARK#2_001", "이미 등록된 즐겨찾기입니다.", HttpStatus.CONFLICT);
      default:
        break;
    }

    Route route = routeRepository.findById(routeId)
        .orElseThrow(
            () -> new CustomException("BOOKMARK#1_002", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    Bookmark bookmark = Bookmark.createBookmark(referenceUser, route);
    return BookmarkCreateResponse.from(bookmarkRepository.save(bookmark));
  }

  @Transactional(readOnly = true)
  public Page<BookmarkListResponse> searchBookmarks(AuthUserInfo user, int page, int size) {
    User referenceUser = userRepository.getReferenceById(user.getId());
    
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    return bookmarkRepository.getUserBookmarks(referenceUser.getId(), pageRequest);
  }

  @Transactional
  public void deleteBookmark(Long bookmarkId, AuthUserInfo user) {
    User referenceUser = userRepository.getReferenceById(user.getId());

    Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
        .orElseThrow(
            () -> new CustomException("BOOKMARK#1_003", "해당 즐겨찾기를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND));

    if (!bookmark.getUser().getId().equals(referenceUser.getId())) {
      throw new CustomException("BOOKMARK#3_001", "본인의 즐겨찾기만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
    }

    bookmarkRepository.delete(bookmark);
  }

  @Transactional(readOnly = true)
  public Page<BookmarkRankingResponse> getMonthlyRankings(int year, int month, int page, int size) {
    validateYearAndMonth(year, month);

    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return bookmarkRepository.findMonthlyRankings(year, month, pageRequest);
  }

  @Transactional(readOnly = true)
  public Page<BookmarkRankingResponse> getRealtimeRankings(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return bookmarkRepository.findRealtimeRankings(pageRequest);
  }

  @Transactional(readOnly = true)
  public Page<BookmarkRankingResponse> getAlltimeRankings(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return bookmarkRepository.findAlltimeRankings(pageRequest);
  }

  private void validateYearAndMonth(int year, int month) {
    if (month < 1 || month > 12) {
      throw new CustomException("BOOKMARK#4_001", "월은 1~12 사이여야 합니다.", HttpStatus.BAD_REQUEST);
    }

    YearMonth inputDate = YearMonth.of(year, month);
    if (inputDate.isAfter(YearMonth.now())) {
      throw new CustomException("BOOKMARK#4_002", "요청하신 날짜는 현재 날짜보다 미래일 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
  }
}
