package com.sparta.travelconquestbe.api.route.dto.request;

import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.common.annotation.UniqueLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RouteCreateRequest {
  @NotBlank(message = "생성할 루트의 이름을 입력해주세요.")
  private String title;

  @NotBlank(message = "생성할 루트의 설명을 입력해주세요.")
  private String description;

  private Long totalDistance;
  private int money;
  private String estimatedTime;

  @NotNull(message = "선택된 경로가 없습니다. 장소를 1개이상 선택해주세요.")
  @Size(min = 2, max = 15, message = "장소를 2개이상 15개 이하로 선택해주세요.")
  @UniqueLocation
  @Valid
  private List<RouteLocationInfo> locations;
}
