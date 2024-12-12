package com.sparta.travelconquestbe.domain.route.repository;

import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.domain.route.enums.RouteSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RouteRepositoryQueryDsl {

    Page<RouteSearchAllResponse> routeSearchAll(Pageable pageable, RouteSort sort);
}
