package com.sparta.travelconquestbe.common.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchUtil {
  private final Set<String> addressCodeSet = ConcurrentHashMap.newKeySet();

  public boolean isBusiness(int businessStatusCode) {
    return businessStatusCode == 1;
  }

  public boolean isValidNumber(String value) {
    try {
      new BigDecimal(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public boolean isDuplicate(BigDecimal latitude, BigDecimal longitude, String locationName) {
    String key = locationName + "_" + latitude + "_" + longitude;
    boolean duplicate = addressCodeSet.add(key);
    if (!duplicate) {
      log.info("Duplicate address code = {}", key);
    }
    return !duplicate;
  }

  public LocalDate parseLocalDate(String dateTime) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      return LocalDate.parse(dateTime, formatter);
    } catch (DateTimeParseException e) {
      log.error("날짜 데이터 형식 오류 = {}", dateTime);
      throw new IllegalArgumentException("날짜 형식 오류: " + dateTime, e);
    }
  }

  public double[] convertUTMToLatLon(double utmX, double utmY) {
    CRSFactory factory = new CRSFactory();
    CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:5174");
    CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");

    BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
    ProjCoordinate utmCoord = new ProjCoordinate(utmX, utmY);
    ProjCoordinate latLonCoord = new ProjCoordinate();

    transform.transform(utmCoord, latLonCoord);

    // 위도와 경도를 BigDecimal로 변환 후 소수점 8자리로 설정
    BigDecimal latitude = new BigDecimal(latLonCoord.y).setScale(8, RoundingMode.HALF_UP);
    BigDecimal longitude = new BigDecimal(latLonCoord.x).setScale(8, RoundingMode.HALF_UP);

    return new double[] {latitude.doubleValue(), longitude.doubleValue()};
  }
}
