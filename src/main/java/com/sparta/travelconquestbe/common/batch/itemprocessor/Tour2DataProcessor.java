//package com.sparta.travelconquestbe.common.batch.itemprocessor;
//
//import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
//import com.sparta.travelconquestbe.common.exception.BusinessStatusSkipException;
//import com.sparta.travelconquestbe.common.exception.DuplicateLocationException;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.file.transform.FieldSet;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class Tour2DataProcessor implements ItemProcessor<FieldSet, LocationData> {
//  private final BatchUtil batchUtil;
//
//  @Override
//  public LocationData process(FieldSet fieldSet) {
//    String locationName = fieldSet.readString(21);
//    String baseDate = fieldSet.readString(24);
//    String latitudeString = fieldSet.readString(26);
//    String longitudeString = fieldSet.readString(27);
//    String address =
//        fieldSet.readString(18).isEmpty() ? fieldSet.readString(19) : fieldSet.readString(18);
//
//    int businessStatusCode = fieldSet.readInt(7);
//    if (!batchUtil.isBusiness(businessStatusCode)) {
//      throw new BusinessStatusSkipException("영업중이 아닌 장소불가");
////      return null;
//    }
////
////    if (batchUtil.isDuplicate(address, locationName)) {
//////      throw new DuplicateLocationException("동일한 장소 추가불가");
////      return null;
////    }
//
//    LocalDate parsedDate = batchUtil.parseLocalDate(baseDate);
//
//    BigDecimal utmX =
//        batchUtil.isValidNumber(latitudeString) ? new BigDecimal(latitudeString) : BigDecimal.ZERO;
//    BigDecimal utmY =
//        batchUtil.isValidNumber(longitudeString)
//            ? new BigDecimal(longitudeString)
//            : BigDecimal.ZERO;
//
//    double[] latLon = batchUtil.convertUTMToLatLon(utmX.doubleValue(), utmY.doubleValue());
//
//    return new LocationData(
//        locationName,
//        BigDecimal.valueOf(latLon[0]),
//        BigDecimal.valueOf(latLon[1]),
//        parsedDate,
//        address);
//  }
//}
