package com.sparta.travelconquestbe.domain.route.repository;

import com.sparta.travelconquestbe.domain.route.entity.Route;
import jakarta.transaction.Transactional;
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
}
