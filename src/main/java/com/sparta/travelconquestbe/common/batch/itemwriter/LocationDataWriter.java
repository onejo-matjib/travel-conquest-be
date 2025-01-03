package com.sparta.travelconquestbe.common.batch.itemwriter;

import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationDataWriter implements ItemWriter<LocationData> {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void write(Chunk<? extends LocationData> chunk) {
    List<? extends LocationData> items = chunk.getItems();
    write(items);
  }

  public void write(List<? extends LocationData> items) {
    String sql =
        "INSERT INTO location_data (location_name, base_date, latitude, longitude,address) VALUES (?, ?, ?, ?, ?)";

    List<Object[]> batchArgs =
        items.stream()
            .map(
                item ->
                    new Object[] {
                      item.getLocationName(),
                      item.getBaseDate(),
                      item.getLatitude(),
                      item.getLongitude(),
                      item.getAddress()
                    })
            .toList();

    jdbcTemplate.batchUpdate(sql, batchArgs);
  }
}
