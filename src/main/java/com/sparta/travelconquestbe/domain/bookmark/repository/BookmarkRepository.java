package com.sparta.travelconquestbe.domain.bookmark.repository;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  @Query("SELECT EXISTS (SELECT 1 FROM Bookmark b WHERE b.user.id = :userId AND b.route.id = :routeId)")
  boolean isBookmarkExist(@Param("userId") Long userId, @Param("routeId") Long routeId);

  @Query("SELECT new com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse(" +
      "b.id, b.route.id, b.route.title, b.createdAt) " +
      "FROM Bookmark b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
  Page<BookmarkListResponse> getUserBookmarks(@Param("userId") Long userId, Pageable pageable);
}
