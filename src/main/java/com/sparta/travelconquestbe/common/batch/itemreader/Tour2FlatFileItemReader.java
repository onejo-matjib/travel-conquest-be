package com.sparta.travelconquestbe.common.batch.itemreader;

import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
import com.sparta.travelconquestbe.common.exception.BusinessStatusSkipException;
import com.sparta.travelconquestbe.common.exception.DuplicateLocationException;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;

@RequiredArgsConstructor
public class Tour2FlatFileItemReader extends FlatFileItemReader<LocationData> {

  private final BatchUtil batchUtil;

  public FlatFileItemReader<LocationData> tour2flatFileItemReader() {
    FlatFileItemReader<LocationData> reader = new FlatFileItemReader<>();
    reader.setLinesToSkip(1);
    reader.setEncoding("EUC-KR");

    DefaultLineMapper<LocationData> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
    lineMapper.setFieldSetMapper(this::mapFor63Column);
    reader.setLineMapper(lineMapper);
    return reader;
  }

  private LocationData mapFor63Column(FieldSet fieldSet) {
    String locationName = fieldSet.readString(21);
    String baseDate = fieldSet.readString(24);
    String latitudeString = fieldSet.readString(26);
    String longitudeString = fieldSet.readString(27);
    String address =
        fieldSet.readString(18).isEmpty() ? fieldSet.readString(19) : fieldSet.readString(18);

    int businessStatusCode = fieldSet.readInt(7);
    if (!batchUtil.isBusiness(businessStatusCode)) {
      throw new BusinessStatusSkipException("영업중이 아닌 장소");
    }

    LocalDate parsedDate = batchUtil.parseLocalDate(baseDate);

    BigDecimal utmX =
        batchUtil.isValidNumber(latitudeString) ? new BigDecimal(latitudeString) : BigDecimal.ZERO;
    BigDecimal utmY =
        batchUtil.isValidNumber(longitudeString)
            ? new BigDecimal(longitudeString)
            : BigDecimal.ZERO;

    if (batchUtil.isDuplicate(utmX, utmY, locationName)) {
      throw new DuplicateLocationException("동일한 장소 추가");
    }

    double[] latLon = batchUtil.convertUTMToLatLon(utmX.doubleValue(), utmY.doubleValue());

    return new LocationData(
        locationName,
        BigDecimal.valueOf(latLon[0]),
        BigDecimal.valueOf(latLon[1]),
        parsedDate,
        address);
  }
}
