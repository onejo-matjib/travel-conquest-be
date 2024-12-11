package com.sparta.travelconquestbe.api.route.service;

import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.service.RouteLocationService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.route.repository.RouteRepository;
import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import com.sparta.travelconquestbe.domain.routelocation.repository.RouteLocationRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

  @Transactional
  public RouteCreateResponse routeCreate(
      RouteCreateRequest routeCreateRequest,
      List<RouteLocationInfo> updatedLocations,
      Long userId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException("ROUTE#2_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
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

  @Transactional
  public void routeDelete(Long id, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CustomException("ROUTE#2_002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    Route route =
        routeRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomException("ROUTE#1_002", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    route.validCreatorOrAdmin(user.getId(), user.getType());
    List<String> locationsMediaUrls =
        route.getLocations().stream().map(RouteLocation::getMediaUrl).toList();
    routeLocationService.deleteFilesForLocations(locationsMediaUrls);
    routeRepository.deleteRouteLocationsReviewsAndRoute(route.getId());
  }
}
