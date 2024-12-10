package com.sparta.travelconquestbe.api.route.dto.response;

import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteCreateResponse {
    private String title;
    private String description;
    private Long totalDistance;
    private int money;
    private String estimatedTime;
    private List<RouteLocationInfo> locations;
}
