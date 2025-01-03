package com.sparta.travelconquestbe.common.validator;

import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.common.annotation.ValidInKorea;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class KoreaLatitudeLongitudeValidator
    implements ConstraintValidator<ValidInKorea, RouteLocationInfo> {
  @Override
  public boolean isValid(RouteLocationInfo location, ConstraintValidatorContext context) {
    BigDecimal latitude = location.getLatitude();
    BigDecimal longitude = location.getLongitude();

    // 대한민국 내 위도 및 경도 조합 검사
    boolean isLatitudeValid =
        latitude.compareTo(new BigDecimal("33.0")) >= 0
            && latitude.compareTo(new BigDecimal("38.6")) <= 0;
    boolean isLongitudeValid =
        longitude.compareTo(new BigDecimal("124.0")) >= 0
            && longitude.compareTo(new BigDecimal("132.0")) <= 0;

    return isLatitudeValid && isLongitudeValid;
  }
}
