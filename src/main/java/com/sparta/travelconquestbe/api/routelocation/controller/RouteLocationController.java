package com.sparta.travelconquestbe.api.routelocation.controller;

import com.sparta.travelconquestbe.api.client.dataset.service.DataSetService;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class RouteLocationController {
  private final DataSetService dataSetService;

  @GetMapping
  public ResponseEntity<List<LocationData>> searchTourData(
      @RequestParam BigDecimal latitude,
      @RequestParam BigDecimal longitude,
      @RequestParam(defaultValue = "1000") long radius) // 기본값 1KM
      {
    return ResponseEntity.ok().body(dataSetService.searchLocationsWithinRadius(latitude,longitude,radius));
  }
}
