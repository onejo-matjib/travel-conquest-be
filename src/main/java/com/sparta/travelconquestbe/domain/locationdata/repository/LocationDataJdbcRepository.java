package com.sparta.travelconquestbe.domain.locationdata.repository;

import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.util.List;

public interface LocationDataJdbcRepository {

  void bulkInsertOrUpdate(List<LocationData> locationDataList);

  void bulkDelete(List<LocationData> deleteList);
}
