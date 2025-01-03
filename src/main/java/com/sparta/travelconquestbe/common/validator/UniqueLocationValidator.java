package com.sparta.travelconquestbe.common.validator;

import com.sparta.travelconquestbe.api.routelocation.dto.info.RouteLocationInfo;
import com.sparta.travelconquestbe.common.annotation.UniqueLocation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class UniqueLocationValidator
    implements ConstraintValidator<UniqueLocation, List<RouteLocationInfo>> {
  @Override
  public boolean isValid(List<RouteLocationInfo> locations, ConstraintValidatorContext context) {

    for (int i = 1; i < locations.size(); i++) {
      RouteLocationInfo previous = locations.get(i - 1);
      RouteLocationInfo current = locations.get(i);

      if (previous.getLatitude().compareTo(current.getLatitude()) == 0
          && previous.getLongitude().compareTo(current.getLongitude()) == 0) {
        return false;
      }
    }

    return true;
  }
}
