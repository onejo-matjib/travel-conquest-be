package com.sparta.travelconquestbe.domain.routelocation.repository;

import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RouteLocationJdbcRepositoryImpl implements RouteLocationJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void bulkSave(List<RouteLocation> locations) {
    jdbcTemplate.batchUpdate(
        "INSERT INTO routelocations(created_at, latitude, location_name, longitude, route_id, sequence,media_url) VALUES(?,?,?,?,?,?,?)",
        new BatchPreparedStatementSetter() {

          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            RouteLocation location = locations.get(i);
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, location.getLatitude().toString());
            ps.setString(3, location.getLocationName());
            ps.setString(4, location.getLongitude().toString());
            ps.setString(5, location.getRoute().getId().toString());
            ps.setString(6, String.valueOf(location.getSequence()));
            ps.setString(7, location.getMediaUrl());
          }

          @Override
          public int getBatchSize() {
            return locations.size();
          }
        });
  }
}
