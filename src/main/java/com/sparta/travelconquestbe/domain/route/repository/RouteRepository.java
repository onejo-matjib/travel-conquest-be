package com.sparta.travelconquestbe.domain.route.repository;

import com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteRepository extends JpaRepository<Route, Long>, RouteRepositoryQueryDsl {
  @Modifying
  @Transactional
  @Query(
      value =
          "DELETE  b, rl, r, rt FROM routelocations rl "
              + "LEFT JOIN reviews r ON r.route_id = rl.route_id "
              + "LEFT JOIN bookmarks b ON b.route_id = rl.route_id "
              + "LEFT JOIN routes rt ON rt.id = rl.route_id "
              + "WHERE rl.route_id = :routeId",
      nativeQuery = true)
  void deleteRouteLocationsReviewsAndRoute(@Param("routeId") Long routeId);

  // 월별 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<RouteRankingResponse> findMonthlyRankings(
      @Param("year") int year,
      @Param("month") int month,
      Pageable pageable
  );

  // 이번달 실시간 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          WHERE YEAR(r.createdAt) = YEAR(CURRENT_DATE) AND MONTH(r.createdAt) = MONTH(CURRENT_DATE)
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<RouteRankingResponse> findRealtimeRankings(Pageable pageable);

  // 역대 TOP 100
  @Query("""
          SELECT new com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse(
                 r.user.nickname, r.title, r.description,
                 COALESCE(r.updatedAt, r.createdAt), r.createdAt)
          FROM Bookmark b
          JOIN b.route r
          GROUP BY r.id
          ORDER BY COUNT(b.id) DESC, r.createdAt ASC
      """)
  Page<RouteRankingResponse> findAlltimeRankings(Pageable pageable);
}
