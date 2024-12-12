package com.sparta.travelconquestbe.api.routelocation.dto.info;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RouteLocationInfo {
  private int sequence;
  private String locationName;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private String mediaUrl;
  private String fileName;
}
