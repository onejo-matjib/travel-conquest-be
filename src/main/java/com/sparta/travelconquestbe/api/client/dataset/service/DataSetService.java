package com.sparta.travelconquestbe.api.client.dataset.service;

import com.sparta.travelconquestbe.api.client.dataset.DataSetDeignClient;
import com.sparta.travelconquestbe.api.client.dataset.TourDataSetFeignClient;
import com.sparta.travelconquestbe.api.client.dataset.dto.response.TourGongGongSearchResponse;
import com.sparta.travelconquestbe.api.client.dataset.dto.response.TourLocalSearchResponse;
import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import com.sparta.travelconquestbe.domain.locationdata.repository.LocationDataRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSetService {
  private final LocationDataRepository locationDataRepository;
  private final TourDataSetFeignClient tourDataSetFeignClient;
  private final DataSetDeignClient dataSetDeignClient;
  private final BatchUtil batchUtil;
  private final List<String> opnSvcIds =
      List.of(
          "03_07_01_P",
          "03_07_03_P",
          "03_07_04_P",
          "03_07_05_P",
          "03_07_09_P",
          "03_07_10_P",
          "03_07_11_P",
          "03_07_12_P",
          "03_07_13_P");

  @Value("${gonggong.dataset.servicekey}")
  private String serviceKey;

  @Value("${localdataset.authkey}")
  private String authKey;

  // 반경 N KM 장소 조회 메소드
  public List<LocationData> searchLocationsWithinRadius(
      BigDecimal latitude, BigDecimal longitude, long radiusInMeters) {
    return locationDataRepository.findLocationsWithinRadius(latitude, longitude, radiusInMeters);
  }

  // 로컬데이터셋 변경 데이터 수집 후 데이터 삽입/변경/삭제 처리
  @Transactional
  // 매 새벽 2시 실행
  @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul")
  public void fetchAndProcess() {
    List<TourLocalSearchResponse.Result.Body.RowContainer.Row> insertList = new ArrayList<>();
    List<TourLocalSearchResponse.Result.Body.RowContainer.Row> deleteList = new ArrayList<>();
    List<LocationData> insertList2 =
        this.searchTourDataResponses(LocalDate.now().toString())
            .getResponse()
            .getBody()
            .getItems()
            .stream()
            .map(
                item ->
                    LocationData.builder()
                        .locationName(item.getTrrsrtNm())
                        .address(item.getLnmadr() != null ? item.getLnmadr() : item.getRdnmadr())
                        .latitude(
                            new BigDecimal(item.getLatitude())
                                .setScale(8, BigDecimal.ROUND_HALF_UP))
                        .longitude(
                            new BigDecimal(item.getLongitude())
                                .setScale(8, BigDecimal.ROUND_HALF_UP))
                        .baseDate(LocalDate.parse(item.getReferenceDate()))
                        .build())
            .toList();
    for (String opnSvcId : opnSvcIds) {
      TourLocalSearchResponse datas = searchDataSetResponses(opnSvcId);
      if (datas.getResult().getBody() != null
          && datas.getResult().getBody().getRows().get(0).getRow() != null) {
        datas
            .getResult()
            .getBody()
            .getRows()
            .get(0)
            .getRow()
            .forEach(
                row -> {
                  String updateGbn = row.getUpdateGbn();
                  switch (updateGbn) {
                    case "I":
                      insertList.add(row);
                      break;
                    case "U":
                      String trdStateGbn = row.getTrdStateGbn();
                      if (Integer.parseInt(trdStateGbn) >= 2) {
                        deleteList.add(row);
                      } else {
                        insertList.add(row);
                      }
                      break;
                  }
                });
      }
    }
    this.processLocationData(insertList, deleteList, insertList2);
  }

  // 로컬데이터셋 OPEN API 실행 메소드
  private TourLocalSearchResponse searchDataSetResponses(String opnSvcId) {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();
    ResponseEntity<TourLocalSearchResponse> responseEntity =
        dataSetDeignClient.searchDataSet(
            authKey,
            yesterday.toString().replace("-", ""),
            today.toString().replace("-", ""),
            "500",
            "json",
            opnSvcId);
    return responseEntity.getBody();
  }

  // 공공데이터 OPEN API
  private TourGongGongSearchResponse searchTourDataResponses(String referenceDate) {
    ResponseEntity<TourGongGongSearchResponse> responseEntity =
        tourDataSetFeignClient.searchDataTour(serviceKey, "100", "json", referenceDate);
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      return responseEntity.getBody();
    } else {
      throw new IllegalArgumentException(responseEntity.getStatusCode().toString());
    }
  }

  // DB 실행메소드
  private void processLocationData(
      List<TourLocalSearchResponse.Result.Body.RowContainer.Row> insertList,
      List<TourLocalSearchResponse.Result.Body.RowContainer.Row> deleteList,
      List<LocationData> insertList2) {

    List<LocationData> convertInsertList = this.convertToLocationDataList(insertList);
    List<LocationData> convertDeleteList = this.convertToLocationDataList(deleteList);
    locationDataRepository.bulkInsertOrUpdate(convertInsertList);
    locationDataRepository.bulkInsertOrUpdate(insertList2);
    locationDataRepository.bulkDelete(convertDeleteList);
  }

  // API 결과값 Entity 변환 메소드
  private List<LocationData> convertToLocationDataList(
      List<TourLocalSearchResponse.Result.Body.RowContainer.Row> list) {
    return list.stream().map(this::convertRowToLocationData).toList();
  }

  private LocationData convertRowToLocationData(
      TourLocalSearchResponse.Result.Body.RowContainer.Row row) {
    // 죄표 미등록 데이터시 기본 좌표 등록
    double[] latLon = {0.0, 0.0};

    if (row.getX() != null
        && !row.getX().trim().isEmpty()
        && row.getY() != null
        && !row.getY().trim().isEmpty()) {
      latLon =
          batchUtil.convertUTMToLatLon(
              Double.parseDouble(row.getX().trim()), Double.parseDouble(row.getY().trim()));
    }
    String dateString = row.getUpdateDt().split(" ")[0];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(dateString, formatter);
    BigDecimal latitude = new BigDecimal(latLon[0]).setScale(8, BigDecimal.ROUND_HALF_UP);
    BigDecimal longitude = new BigDecimal(latLon[1]).setScale(8, BigDecimal.ROUND_HALF_UP);

    return LocationData.builder()
        .locationName(row.getBplcNm())
        .baseDate(date)
        .address(row.getSiteWhlAddr() != null ? row.getSiteWhlAddr() : row.getRdnWhlAddr())
        .latitude(latitude)
        .longitude(longitude)
        .build();
  }
}
