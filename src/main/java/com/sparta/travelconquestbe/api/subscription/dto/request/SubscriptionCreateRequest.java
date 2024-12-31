package com.sparta.travelconquestbe.api.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionCreateRequest {

  @NotNull(message = "구독 대상 ID는 필수입니다.")
  private Long subUserId;
}
