package com.sparta.travelconquestbe.api.client.kakao;

import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationSearchRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "kakaoMapClient", url = "https://apis-navi.kakaomobility.com")
public interface KakaoFeignClient {

  @PostMapping(value = "/v1/waypoints/directions", consumes = "application/json")
  ResponseEntity<RouteLineResponse> searchRouteLine(
      @RequestHeader("Authorization") String authorization,
      @RequestBody LocationSearchRequest location);

  @GetMapping(value = "/v1/directions", consumes = "application/json")
  ResponseEntity<RouteLineResponse> searchRouteLinesDetails(
      @RequestHeader("Authorization") String authorization,
      @RequestParam("origin") String origin,
      @RequestParam("destination") String destination);
}
