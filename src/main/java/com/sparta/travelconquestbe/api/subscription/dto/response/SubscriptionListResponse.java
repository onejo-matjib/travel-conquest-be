package com.sparta.travelconquestbe.api.subscription.dto.response;

import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class SubscriptionListResponse {

  private Long totalFollowings;
  private List<FollowingResponse> followings;

  public static SubscriptionListResponse from(Page<Subscription> subscriptions,
      Long totalFollowings) {
    return SubscriptionListResponse.builder()
        .totalFollowings(totalFollowings)
        .followings(subscriptions.stream()
            .map(FollowingResponse::from)
            .collect(Collectors.toList()))
        .build();
  }

  @Getter
  @Builder
  public static class FollowingResponse {

    private Long id;
    private Long subUserId;

    public static FollowingResponse from(Subscription subscription) {
      return FollowingResponse.builder()
          .id(subscription.getId())
          .subUserId(subscription.getSubUserId())
          .build();
    }
  }
}
