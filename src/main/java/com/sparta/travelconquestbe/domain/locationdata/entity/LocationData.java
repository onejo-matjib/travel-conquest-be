package com.sparta.travelconquestbe.domain.locationdata.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// 이름+좌표 조합 유니크/인덱스 설정
@Table(
    name = "location_data",
    indexes = {@Index(name = "idx_location_coordinates", columnList = "latitude, longitude")},
    uniqueConstraints = {
      @UniqueConstraint(
          name = "unique_location_coordinates",
          columnNames = {"location_name", "latitude", "longitude"})
    })
@Getter
@Setter
@NoArgsConstructor
public class LocationData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String locationName;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
  private BigDecimal latitude; // 위도

  @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
  private BigDecimal longitude; // 경도

  private LocalDate baseDate;

  private String address;

  @Builder
  public LocationData(
      String locationName,
      BigDecimal latitude,
      BigDecimal longitude,
      LocalDate baseDate,
      String address) {
    this.locationName = locationName;
    this.latitude = latitude;
    this.longitude = longitude;
    this.baseDate = baseDate;
    this.address = address;
  }
}
