package com.sparta.travelconquestbe.api.client.kakao;

import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoMapClient", url = "https://apis-navi.kakaomobility.com")
public interface KakaoFeignClient {

  @PostMapping(value = "/v1/waypoints/directions", consumes = "application/json")
  ResponseEntity<RouteLineResponse> searchRouteLine(
      @RequestHeader("Authorization") String authorization,
      @RequestBody LocationRequestDTO location);
}
