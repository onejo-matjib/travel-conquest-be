package com.sparta.travelconquestbe.api.route.controller;

import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteLineResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteRankingResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchResponse;
import com.sparta.travelconquestbe.api.route.service.RouteService;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.service.RouteLocationService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.route.enums.RouteSort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/routes")
@Validated
public class RouteController {

  private final RouteService routeService;
  private final RouteLocationService routeLocationService;

  @PostMapping()
  public ResponseEntity<RouteCreateResponse> routeCreate(
      @Valid @RequestPart(value = "routeCreateRequest") RouteCreateRequest routeCreateRequest,
      @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
      @AuthUser AuthUserInfo userId)
      throws Exception {

    List<RouteLocationInfo> updatedLocations =
        routeLocationService.uploadFilesForLocations(routeCreateRequest, mediaFiles);

    RouteCreateResponse response =
        routeService.routeCreate(routeCreateRequest, updatedLocations, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<Page<RouteSearchAllResponse>> routeSearchAll(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit,
      @ValidEnum(enumClass = RouteSort.class, message = "정렬할 컬럼값을 정확하게 입력해주세요.")
          @RequestParam(defaultValue = "UPDATE_AT")
          String sort) {
    RouteSort routeSort = RouteSort.valueOf(sort.toUpperCase());
    return ResponseEntity.status(HttpStatus.OK)
        .body(routeService.routeSearchAll(page, limit, routeSort));
  }

  @GetMapping("/search")
  public ResponseEntity<Page<RouteSearchAllResponse>> routeSearchByKeyword(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit,
      @ValidEnum(enumClass = RouteSort.class, message = "정렬할 컬럼값을 정확하게 입력해주세요.")
          @RequestParam(defaultValue = "UPDATE_AT")
          String sort,
      @NotBlank(message = "검색할 단어를 입력해주세요.")
          @Size(min = 2, max = 10, message = "검색어는 2글자 이상 10글자 이하로 검색해주세요.")
          @RequestParam
          String keyword) {
    RouteSort routeSort = RouteSort.valueOf(sort.toUpperCase());
    return ResponseEntity.status(HttpStatus.OK)
        .body(routeService.routeSearchByKeyword(page, limit, routeSort, keyword));
  }

  @GetMapping("/{id}")
  public ResponseEntity<RouteSearchResponse> routeSearch(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(routeService.routeSearch(id));
  }

  @GetMapping("/{id}/origin/{originSequence}/destination/{destinationSequence}")
  public ResponseEntity<RouteLineResponse> routeSearchDetails(
      @PathVariable Long id,
      @Positive(message = "장소 순번을 양수로 입력해주세요.") @PathVariable Long originSequence,
      @Positive(message = "장소 순번을 양수로 입력해주세요.") @PathVariable Long destinationSequence) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(routeService.routeSearchDetails(id, originSequence, destinationSequence));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> routeDelete(@PathVariable Long id, @AuthUser AuthUserInfo user) {
    routeService.routeDelete(id, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  // 월별 TOP 100
  @GetMapping("/rankings/monthly")
  public ResponseEntity<Page<RouteRankingResponse>> getMonthlyRankings(
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam int year,
      @Range(min = 1, max = 12, message = "1월 ~ 12월 사이로 입력해주세요.") @RequestParam int month,
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "1") int page,
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "10") int size) {
    Page<RouteRankingResponse> rankings = routeService.getMonthlyRankings(year, month, page, size);
    return ResponseEntity.ok(rankings);
  }

  // 이번 달 실시간 TOP 100
  @GetMapping("/rankings/realtime")
  public ResponseEntity<Page<RouteRankingResponse>> getRealtimeRankings(
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "1") int page,
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "10") int size) {
    Page<RouteRankingResponse> rankings = routeService.getRealtimeRankings(page, size);
    return ResponseEntity.ok(rankings);
  }

  // 역대 TOP 100
  @GetMapping("/rankings/alltime")
  public ResponseEntity<Page<RouteRankingResponse>> getAlltimeRankings(
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "1") int page,
      @Positive(message = "양수만 입력 가능합니다.") @RequestParam(defaultValue = "10") int size) {
    Page<RouteRankingResponse> rankings = routeService.getAlltimeRankings(page, size);
    return ResponseEntity.ok(rankings);
  }
}
