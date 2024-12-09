package com.sparta.travelconquestbe.api.bookmark.controller;

import com.sparta.travelconquestbe.api.bookmark.dto.request.BookmarkCreateRequest;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkCreateResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.service.BookmarkService;
import com.sparta.travelconquestbe.common.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @PostMapping
  public ResponseEntity<BookmarkCreateResponse> createBookmark(
      @Valid @RequestBody BookmarkCreateRequest request,
      AuthUser authUser) {
    BookmarkCreateResponse response = bookmarkService.createBookmark(request.getRouteId(),
        authUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<Page<BookmarkListResponse>> getBookmarks(Pageable pageable,
      AuthUser authUser) {
    Page<BookmarkListResponse> response = bookmarkService.getBookmarks(authUser, pageable);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBookmark(@PathVariable Long id, AuthUser authUser) {
    bookmarkService.deleteBookmark(id, authUser);
    return ResponseEntity.noContent().build();
  }
}
