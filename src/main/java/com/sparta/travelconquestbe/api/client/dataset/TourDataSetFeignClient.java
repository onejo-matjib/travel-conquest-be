package com.sparta.travelconquestbe.api.client.dataset;

import com.sparta.travelconquestbe.api.client.dataset.dto.response.TourGongGongSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dataSetClient", url = "http://api.data.go.kr/openapi")
public interface TourDataSetFeignClient {

  @GetMapping(
      value = "/tn_pubr_public_trrsrt_api",
      produces = "application/json",
      consumes = "application/json")
  ResponseEntity<TourGongGongSearchResponse> searchDataTour(
      @RequestParam("serviceKey") String serviceKey,
      @RequestParam(value = "numOfRows", defaultValue = "100") String numOfRows,
      @RequestParam("type") String type,
      @RequestParam("referenceDate") String referenceDate);
}
