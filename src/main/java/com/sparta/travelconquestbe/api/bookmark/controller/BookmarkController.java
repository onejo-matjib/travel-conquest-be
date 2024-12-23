package com.sparta.travelconquestbe.api.bookmark.controller;

import com.sparta.travelconquestbe.api.bookmark.dto.request.BookmarkCreateRequest;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Validated
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @PostMapping
  public ResponseEntity<BookmarkCreateResponse> createBookmark(
      @Valid @RequestBody BookmarkCreateRequest request,
      @AuthUser AuthUserInfo user
  ) {
    BookmarkCreateResponse response = bookmarkService.createBookmark(request.getRouteId(), user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<Page<BookmarkListResponse>> searchBookmarks(
      @Positive @RequestParam(defaultValue = "1") int page,
      @Positive @RequestParam(defaultValue = "10") int size,
      @AuthUser AuthUserInfo user
  ) {
    Page<BookmarkListResponse> response = bookmarkService.searchBookmarks(user, page, size);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBookmark(
      @PathVariable Long id,
      @AuthUser AuthUserInfo user
  ) {
    bookmarkService.deleteBookmark(id, user);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/all")
  public ResponseEntity<List<BookmarkListResponse>> searchAllBookmarksForLocalStorage(
      @AuthUser AuthUserInfo user
  ) {
    List<BookmarkListResponse> allBookmarks = bookmarkService.searchAllBookmarksForLocalStorage(
        user);
    return ResponseEntity.ok(allBookmarks);
  }
}
