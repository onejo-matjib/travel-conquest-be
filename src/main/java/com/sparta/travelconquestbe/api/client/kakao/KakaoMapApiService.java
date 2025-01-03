package com.sparta.travelconquestbe.api.client.kakao;

import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationDetailsRequest;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationSearchRequest;
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

  private String getAuthorization() {
    return "KakaoAK " + apiKey;
  }

  public RouteLineResponse searchRouteLine(LocationSearchRequest location) {
    ResponseEntity<RouteLineResponse> response =
        kakaoFeignClient.searchRouteLine(getAuthorization(), location);

    return response.getBody();
  }

  public RouteLineResponse searchRouteLinesDetails(LocationDetailsRequest locationDetailsRequest) {
    ResponseEntity<RouteLineResponse> response =
        kakaoFeignClient.searchRouteLinesDetails(
            getAuthorization(),
            locationDetailsRequest.getOrigin(),
            locationDetailsRequest.getDestination());
    return response.getBody();
  }
}
