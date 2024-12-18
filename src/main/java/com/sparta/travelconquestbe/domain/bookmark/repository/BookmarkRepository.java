package com.sparta.travelconquestbe.domain.bookmark.repository;

import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse;
import com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkRankingResponse;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  @Query(value = """
          SELECT CASE 
              WHEN NOT EXISTS (SELECT 1 FROM routes WHERE id = :routeId) THEN 'ROUTE_NOT_FOUND'
              WHEN EXISTS (SELECT 1 FROM bookmarks WHERE user_id = :userId AND route_id = :routeId) THEN 'DUPLICATE_BOOKMARK'
              ELSE 'VALID'
          END AS validation_result
          FROM dual
      """, nativeQuery = true)
  String validateBookmarkCreation(@Param("userId") Long userId, @Param("routeId") Long routeId);

  @Query("SELECT new com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkListResponse(" +
      "b.id, b.route.id, b.route.title, b.createdAt) " +
      "FROM Bookmark b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
  Page<BookmarkListResponse> getUserBookmarks(@Param("userId") Long userId, Pageable pageable);

  // 월별 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<BookmarkRankingResponse> findMonthlyRankings(
      @Param("year") int year,
      @Param("month") int month,
      Pageable pageable
  );

  // 이번달 실시간 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          WHERE YEAR(r.createdAt) = YEAR(CURRENT_DATE) AND MONTH(r.createdAt) = MONTH(CURRENT_DATE)
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<BookmarkRankingResponse> findRealtimeRankings(Pageable pageable);

  // 역대 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.bookmark.dto.response.BookmarkRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<BookmarkRankingResponse> findAlltimeRankings(Pageable pageable);
}
