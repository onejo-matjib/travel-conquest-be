package com.sparta.travelconquestbe.api.route.controller;

import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.api.route.service.RouteService;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.service.RouteLocationService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.domain.route.enums.RouteSort;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
      @AuthUser Long userId)
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

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> routeDelete(@PathVariable Long id, @AuthUser Long userId) {
    routeService.routeDelete(id, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
