package com.sparta.travelconquestbe.api.client.kakao;

import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoMapApiService {
  private final KakaoFeignClient kakaoFeignClient;

  @Value("${kakao.client-id}")
  private String apiKey;

  public RouteLineResponse searchRouteLine(LocationRequestDTO location) {
    String authorization = "KakaoAK " + apiKey;
    ResponseEntity<RouteLineResponse> response =
        kakaoFeignClient.searchRouteLine(authorization, location);

    return response.getBody();
  }
}
