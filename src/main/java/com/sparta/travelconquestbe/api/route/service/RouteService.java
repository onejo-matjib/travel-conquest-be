package com.sparta.travelconquestbe.api.route.service;

import com.sparta.travelconquestbe.api.client.kakao.KakaoMapApiService;
import com.sparta.travelconquestbe.api.review.dto.respones.ReviewSearchResponse;
import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchResponse;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.dto.request.LocationDetailsRequest;
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
import jakarta.validation.constraints.Positive;
import java.time.YearMonth;
import java.util.List;
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

  @Transactional(readOnly = true)
  public Page<RouteSearchAllResponse> routeSearchByKeyword(
      @Positive int page, @Positive int limit, RouteSort sort, String keyword) {
    Pageable pageable = PageRequest.of(page - 1, limit);
    return routeRepository.routeSearchByKeyword(pageable, sort, keyword);
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
        kakaoMapApiService.searchRouteLine(buildLocationSearchAllRequest(locations));

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

  @Transactional(readOnly = true)
  public RouteLineResponse routeSearchDetails(
      Long id, Long originSequence, Long destinationSequence) {
    Route route =
        routeRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new CustomException("ROUTE#1_004", "해당 루트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    List<RouteLocation> locations = route.getLocations();
    return kakaoMapApiService.searchRouteLinesDetails(
        buildLocationSearchDetailsRequest(locations, originSequence, destinationSequence));
  }

  private LocationSearchRequest buildLocationSearchAllRequest(List<RouteLocation> locations) {
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
                .toList())
        .build();
  }

  private LocationDetailsRequest buildLocationSearchDetailsRequest(
      List<RouteLocation> locations, Long originSequence, Long destinationSequence) {
    if (originSequence >= locations.size() || destinationSequence > locations.size()) {
      throw new CustomException("ROUTE#2_002", "해당 장소가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    RouteLocation originLocation = locations.get((int) (originSequence - 1));
    RouteLocation destinationLocation = locations.get((int) (destinationSequence - 1));

    String origin = originLocation.getLongitude() + "," + originLocation.getLatitude();
    String destination =
        destinationLocation.getLongitude() + "," + destinationLocation.getLatitude();
    return LocationDetailsRequest.builder().origin(origin).destination(destination).build();
  }

  @Transactional(readOnly = true)
  public Page<RouteRankingResponse> getMonthlyRankings(int year, int month, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return routeRepository.findMonthlyRankings(year, month, pageRequest);
  }

  @Transactional(readOnly = true)
  public Page<RouteRankingResponse> getRealtimeRankings(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return routeRepository.findRealtimeRankings(pageRequest);
  }

  @Transactional(readOnly = true)
  public Page<RouteRankingResponse> getAlltimeRankings(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page - 1, Math.min(size, 10));
    return routeRepository.findAlltimeRankings(pageRequest);
  }

  private void validateYearAndMonth(int year, int month) {
    if (month < 1 || month > 12) {
      throw new CustomException("BOOKMARK#4_001", "월은 1~12 사이여야 합니다.", HttpStatus.BAD_REQUEST);
    }

    YearMonth inputDate = YearMonth.of(year, month);
    if (inputDate.isAfter(YearMonth.now())) {
      throw new CustomException("BOOKMARK#4_002", "요청하신 날짜는 현재 날짜보다 미래일 수 없습니다.",
          HttpStatus.BAD_REQUEST);
    }
  }

  //추가함
  @Transactional
  public Route createRouteDirectly(RouteCreateRequest routeCreateRequest, User user) {
    // 등급 업 신청 시 전달받은 route 정보로 바로 Route 생성
    // 기존 routeCreate 로직 재활용 가능
    // media 업로드가 필요없다고 가정하거나, 기존 로직 사용
    // 여기서는 간략하게 만 표시중...

    Route route = Route.builder()
        .title(routeCreateRequest.getTitle())
        .description(routeCreateRequest.getDescription())
        .estimatedTime(routeCreateRequest.getEstimatedTime())
        .money(routeCreateRequest.getMoney())
        .totalDistance(routeCreateRequest.getTotalDistance())
        .user(user)
        .build();

    Route savedRoute = routeRepository.save(route);
    // 위치 정보도 반영 필요하면 기존 bulkSave로 locations 처리
    // 여기서는 단순히 등급업 신청을 위한 Route라고 가정하에 로직을 짯습니다.

    return savedRoute;
  }
}
