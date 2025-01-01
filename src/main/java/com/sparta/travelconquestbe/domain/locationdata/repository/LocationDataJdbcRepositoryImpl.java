package com.sparta.travelconquestbe.domain.locationdata.repository;

import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationDataJdbcRepositoryImpl implements LocationDataJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void bulkInsertOrUpdate(List<LocationData> locationDataList) {
    String sql =
        "INSERT INTO location_data (location_name, address, base_date, latitude, longitude) "
            + "VALUES (?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE location_name = VALUES(location_name), address = VALUES(address), base_date = VALUES(base_date)";

    jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            LocationData location = locationDataList.get(i);
            ps.setString(1, location.getLocationName());
            ps.setString(2, location.getAddress());
            ps.setDate(3, Date.valueOf(location.getBaseDate()));
            ps.setBigDecimal(4, location.getLatitude());
            ps.setBigDecimal(5, location.getLongitude());
          }

          @Override
          public int getBatchSize() {
            return locationDataList.size();
          }
        });
  }

  @Override
  public void bulkDelete(List<LocationData> deleteList) {
    String sql = "DELETE FROM location_data WHERE latitude=? AND longitude=?";
    jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {

          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            LocationData location = deleteList.get(i);
            ps.setBigDecimal(1, location.getLatitude());
            ps.setBigDecimal(2, location.getLongitude());
          }

          @Override
          public int getBatchSize() {
            return deleteList.size();
          }
        });
  }
}
