package com.sparta.travelconquestbe.api.routelocation.dto.info;

import com.sparta.travelconquestbe.common.annotation.ValidInKorea;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ValidInKorea
public class RouteLocationInfo {
  @Min(value = 1, message = "장소 순서를 올바르게 입력해주세요.")
  private int sequence;

  @NotBlank(message = "장소명 입력은 필수 입니다.")
  private String locationName;

  private BigDecimal latitude;
  private BigDecimal longitude;

  private String mediaUrl;

  @NotBlank(message = "파일명을 장소와 맞게 입력해주세요.")
  private String fileName;
}
