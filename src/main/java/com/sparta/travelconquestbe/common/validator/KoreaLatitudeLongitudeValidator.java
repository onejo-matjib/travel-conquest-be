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
    // 대한민국 내 위도 범위 검사
    boolean isLatitudeValid =
        location.getLatitude().compareTo(new BigDecimal(36.0)) >= 0
            && location.getLatitude().compareTo(new BigDecimal(38.6)) <= 0;

    // 대한민국 내 경도 범위 검사
    boolean isLongitudeValid =
        location.getLongitude().compareTo(new BigDecimal(126.0)) >= 0
            && location.getLongitude().compareTo(new BigDecimal(130.5)) <= 0;

    return isLatitudeValid && isLongitudeValid;
  }
}
