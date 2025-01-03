package com.sparta.travelconquestbe.api.client.dataset;

import com.sparta.travelconquestbe.api.client.dataset.dto.response.TourLocalSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "localDataApiClient", url = "http://www.localdata.go.kr/platform/rest")
public interface DataSetDeignClient {
  @GetMapping(value = "/TO0/openDataApi", consumes = "application/json")
  ResponseEntity<TourLocalSearchResponse> searchDataSet(
      @RequestParam("authKey") String authKey,
      @RequestParam("lastModTsBgn") String lastModTsBgn,
      @RequestParam("lastModTsEnd") String lastModTsEnd,
      @RequestParam("pageSize") String pageSize,
      @RequestParam("resultType") String resultType,
      @RequestParam("opnSvcId") String opnSvcId);
}
