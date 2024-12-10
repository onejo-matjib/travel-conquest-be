package com.sparta.travelconquestbe.api.subscription.service;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;

  @Transactional
  public SubscriptionCreateResponse createSubscription(Long userId, Long subUserId) {
    if (userId.equals(subUserId)) {
      throw new CustomException("SUBSCRIPTION_001", "본인을 구독할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    String validationResult = subscriptionRepository.validateSubscriptionCreation(userId,
        subUserId);

    switch (validationResult) {
      case "USER_NOT_FOUND":
        throw new CustomException("USER_001", "구독 대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
      case "DUPLICATE_SUBSCRIPTION":
        throw new CustomException("SUBSCRIPTION_002", "이미 구독 중입니다.", HttpStatus.CONFLICT);
      default:
        break;
    }

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    Subscription subscription = Subscription.builder()
        .userId(user.getId())
        .subUserId(subUserId)
        .build();

    return SubscriptionCreateResponse.from(subscriptionRepository.save(subscription));
  }
}
