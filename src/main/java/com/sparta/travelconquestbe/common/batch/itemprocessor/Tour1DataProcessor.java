package com.sparta.travelconquestbe.common.batch.itemprocessor;

import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Tour1DataProcessor implements ItemProcessor<FieldSet, LocationData> {
  private final BatchUtil batchUtil;

  @Override
  public LocationData process(FieldSet fieldSet) {
    String locationName = fieldSet.readString(0);
    String baseDate = fieldSet.readString(19);
    String latitudeString = fieldSet.readString(4);
    String longitudeString = fieldSet.readString(5);
    String address =
        fieldSet.readString(3).isEmpty() ? fieldSet.readString(2) : fieldSet.readString(3);

    BigDecimal latitudeBigDecimal =
        batchUtil.isValidNumber(latitudeString) ? new BigDecimal(latitudeString) : BigDecimal.ZERO;
    BigDecimal longitudeBigDecimal =
        batchUtil.isValidNumber(longitudeString)
            ? new BigDecimal(longitudeString)
            : BigDecimal.ZERO;

    if (batchUtil.isDuplicate(latitudeBigDecimal, longitudeBigDecimal, locationName)) {
      return null;
    }

    return new LocationData(
        locationName, latitudeBigDecimal, longitudeBigDecimal, LocalDate.parse(baseDate), address);
  }
}
