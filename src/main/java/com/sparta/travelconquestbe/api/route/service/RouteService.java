package com.sparta.travelconquestbe.api.route.service;

import com.sparta.travelconquestbe.api.client.kakao.KakaoMapApiService;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewSearchResponse;
import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationSearchRequest;
import com.sparta.travelconquestbe.api.routelocation.dto.respones.LocationSearchResponse;
import com.sparta.travelconquestbe.api.routelocation.service.RouteLocationService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.enums.RouteSort;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import com.sparta.travelconquestbe.domain.routelocation.repository.RouteLocationRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteService {

  private final RouteRepository routeRepository;
  private final RouteLocationRepository routeLocationRepository;
  private final UserRepository userRepository;
  private final RouteLocationService routeLocationService;
  private final KakaoMapApiService kakaoMapApiService;

  @Transactional
  public RouteCreateResponse routeCreate(
      RouteCreateRequest routeCreateRequest,
      List<RouteLocationInfo> updatedLocations,
      AuthUserInfo userInfo) {

    User user = userRepository.getReferenceById(userInfo.getId());

    Route route =
        Route.builder()
            .title(routeCreateRequest.getTitle())
            .description(routeCreateRequest.getDescription())
            .totalDistance(routeCreateRequest.getTotalDistance())
            .money(routeCreateRequest.getMoney())
            .estimatedTime(routeCreateRequest.getEstimatedTime())
            .user(user)
            .build();
    List<RouteLocation> locations =
        routeCreateRequest.getLocations().stream()
            .map(
                location ->
                    RouteLocation.builder()
                        .sequence(location.getSequence())
                        .locationName(location.getLocationName())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .route(route)
                        .mediaUrl(location.getMediaUrl())
                        .build())
            .toList();
    Route savedRoute = routeRepository.save(route);
    routeLocationRepository.bulkSave(locations);
    return RouteCreateResponse.builder()
        .id(savedRoute.getId())
        .title(savedRoute.getTitle())
        .description(savedRoute.getDescription())
        .totalDistance(savedRoute.getTotalDistance())
        .money(savedRoute.getMoney())
        .estimatedTime(savedRoute.getEstimatedTime())
        .locations(updatedLocations)
        .build();
  }

  @Transactional(readOnly = true)
  public Page<RouteSearchAllResponse> routeSearchAll(int page, int limit, RouteSort sort) {
    Pageable pageable = PageRequest.of(page - 1, limit);
    return routeRepository.routeSearchAll(pageable, sort);
  }

  @Transactional
  public void routeDelete(Long id, AuthUserInfo user) {
    Route route =
        routeRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomException("ROUTE#1_003", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    route.validCreatorOrAdmin(user.getId(), user.getType());
    List<String> locationsMediaUrls =
        route.getLocations().stream().map(RouteLocation::getMediaUrl).toList();
    routeLocationService.deleteFilesForLocations(locationsMediaUrls);
    routeRepository.deleteRouteLocationsReviewsAndRoute(route.getId());
  }

  @Transactional(readOnly = true)
  public RouteSearchResponse routeSearch(Long id) {
    Route route =
        routeRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomException("ROUTE#1_004", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    List<RouteLocation> locations = route.getLocations();

    RouteLineResponse routeLine =
        kakaoMapApiService.searchRouteLine(buildLocationSearchRequest(locations));

    return RouteSearchResponse.builder()
        .title(route.getTitle())
        .description(route.getDescription())
        .totalDistance(route.getTotalDistance())
        .money(route.getMoney())
        .estimatedTime(route.getEstimatedTime())
        .creator(route.getUser().getNickname())
        .routeLine(routeLine)
        .reviews(
            (route.getReviews().stream()
                .map(
                    review ->
                        ReviewSearchResponse.builder()
                            .rating(review.getRating())
                            .comment(review.getComment())
                            .nickname(review.getUser().getNickname())
                            .build())
                .toList()))
        .routeLocations(
            route.getLocations().stream()
                .map(
                    location ->
                        LocationSearchResponse.builder()
                            .sequence(location.getSequence())
                            .locationName(location.getLocationName())
                            .mediaUrl(location.getMediaUrl())
                            .build())
                .toList())
        .build();
  }

  private LocationSearchRequest buildLocationSearchRequest(List<RouteLocation> locations) {
    return LocationSearchRequest.builder()
        .origin(
            new LocationSearchRequest.Origin(
                locations.get(0).getLongitude().doubleValue(),
                locations.get(0).getLatitude().doubleValue()))
        .destination(
            new LocationSearchRequest.Destination(
                locations.get(locations.size() - 1).getLongitude().doubleValue(),
                locations.get(locations.size() - 1).getLatitude().doubleValue()))
        .waypoints(
            locations.subList(1, locations.size() - 1).stream()
                .map(
                    location ->
                        new LocationSearchRequest.Waypoint(
                            location.getLocationName(),
                            location.getLongitude().doubleValue(),
                            location.getLatitude().doubleValue()))
                .collect(Collectors.toList()))
        .build();
  }
}
