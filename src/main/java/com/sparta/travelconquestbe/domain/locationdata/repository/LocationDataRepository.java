package com.sparta.travelconquestbe.domain.locationdata.repository;

import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationDataRepository
    extends JpaRepository<LocationData, Long>, LocationDataJdbcRepository {
  // 반경 1KM 내의 장소들을 검색하는 메서드
  @Query(
      "SELECT l FROM LocationData l WHERE "
          + "ST_Distance_Sphere(Point(l.longitude, l.latitude), Point(:longitude, :latitude)) <= :radius")
  List<LocationData> findLocationsWithinRadius(
      @Param("latitude") BigDecimal latitude,
      @Param("longitude") BigDecimal longitude,
      @Param("radius") long radius);
}
