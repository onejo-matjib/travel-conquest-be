package com.sparta.travelconquestbe.domain.routelocation.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routelocations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteLocation extends TimeStampCreated {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String locationName;

  @Column(nullable = false)
  private int sequence;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
  private BigDecimal latitude; // 위도

  @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
  private BigDecimal longitude; // 경도

  private String mediaUrl;

  @ManyToOne
  @JoinColumn(name = "route_id")
  private Route route;
}
