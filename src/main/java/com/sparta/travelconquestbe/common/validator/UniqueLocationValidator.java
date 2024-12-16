package com.sparta.travelconquestbe.common.validator;

import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.common.annotation.UniqueLocation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueLocationValidator
    implements ConstraintValidator<UniqueLocation, List<RouteLocationInfo>> {
  @Override
  public boolean isValid(List<RouteLocationInfo> locations, ConstraintValidatorContext context) {

    Set<String> uniqueCoordinates = new HashSet<>();

    for (RouteLocationInfo location : locations) {
      String coordinateKey =
          location.getLatitude().toString() + "," + location.getLongitude().toString();
      if (!uniqueCoordinates.add(coordinateKey)) {
        return false;
      }
    }

    return true;
  }
}
