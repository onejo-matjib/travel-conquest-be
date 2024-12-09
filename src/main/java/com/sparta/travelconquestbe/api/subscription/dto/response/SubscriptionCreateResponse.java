package com.sparta.travelconquestbe.api.subscription.dto.response;

import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionCreateResponse {

  private Long id;
  private Long subUserId;

  public static SubscriptionCreateResponse from(Subscription subscription) {
    return SubscriptionCreateResponse.builder()
        .id(subscription.getId())
        .subUserId(subscription.getSubUserId())
        .build();
  }
}
