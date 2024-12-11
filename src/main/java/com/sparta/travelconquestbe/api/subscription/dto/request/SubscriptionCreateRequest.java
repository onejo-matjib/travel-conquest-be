package com.sparta.travelconquestbe.api.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionCreateRequest {

  @NotNull(message = "구독 대상 ID는 필수입니다.")
  private Long subUserId;
}
