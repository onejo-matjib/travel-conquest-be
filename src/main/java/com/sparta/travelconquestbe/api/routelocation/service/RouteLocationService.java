package com.sparta.travelconquestbe.api.routelocation.service;

import com.sparta.travelconquestbe.api.client.s3.service.S3Service;
import com.sparta.travelconquestbe.api.route.dto.request.RouteCreateRequest;
import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RouteLocationService {

  private final S3Service s3Service;

  @Transactional
  public List<RouteLocationInfo> uploadFilesForLocations(
      RouteCreateRequest routeCreateRequest, List<MultipartFile> mediaFiles) throws Exception {
    validLocationsMedia(routeCreateRequest, mediaFiles);
    // 검증된 파일 업로드.
    List<RouteLocationInfo> updatedLocations = new ArrayList<>();
    for (RouteLocationInfo locationInfo : routeCreateRequest.getLocations()) {
      String fileName = locationInfo.getFileName();
      MultipartFile mediaFile =
          mediaFiles.stream()
              .filter(file -> fileName != null && fileName.equals(file.getOriginalFilename()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new CustomException(
                          "ROUTE#3_001",
                          "파일 매칭 중 예상치 못한 오류가 발생했습니다.",
                          HttpStatus.INTERNAL_SERVER_ERROR));
      String uniqueFileName = UUID.randomUUID().toString();
      String mediaUrl = s3Service.uploadFile(mediaFile, uniqueFileName);
      locationInfo.setMediaUrl(mediaUrl);

      updatedLocations.add(locationInfo);
    }
    return updatedLocations;
  }

  @Transactional
  public void deleteFilesForLocations(List<String> locationsMediaUrls) {
    s3Service.deleteFile(locationsMediaUrls);
  }

  // 장소에 대한 사진 혹은 영상이 있는지 검증.
  private void validLocationsMedia(
      RouteCreateRequest routeCreateRequest, List<MultipartFile> mediaFiles) {
    for (RouteLocationInfo locationInfo : routeCreateRequest.getLocations()) {
      String fileName = locationInfo.getFileName();
      boolean fileExists =
          mediaFiles != null
              && mediaFiles.stream()
                  .anyMatch(
                      file -> fileName != null && fileName.equals(file.getOriginalFilename()));
      if (!fileExists) {
        throw new CustomException(
            "ROUTE#2_001",
            "장소에 대한 사진 혹은 영상이 존재하지 않습니다. " + "누락된 장소 : " + locationInfo.getLocationName(),
            HttpStatus.BAD_REQUEST);
      }
    }
  }
}
