package com.sparta.travelconquestbe.api.route.controller;

import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.route.dto.response.RouteCreateResponse;
import com.sparta.travelconquestbe.api.route.service.RouteService;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.api.routelocation.service.RouteLocationService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/routes")
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
}
