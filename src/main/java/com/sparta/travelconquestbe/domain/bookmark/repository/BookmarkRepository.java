package com.sparta.travelconquestbe.domain.bookmark.repository;

import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
      "FROM Bookmark b WHERE b.user.id = :userId AND b.route.id = :routeId")
  boolean isBookmarkExist(@Param("userId") Long userId, @Param("routeId") Long routeId);
}
