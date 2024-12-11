package com.sparta.travelconquestbe.api.subscription.service;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;

  @Transactional
  public SubscriptionCreateResponse createSubscription(Long userId, Long subUserId) {
    if (userId.equals(subUserId)) {
      throw new CustomException("SUBSCRIPTION#1_001", "본인을 구독할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    String validationResult = subscriptionRepository.validateSubscriptionCreation(userId,
        subUserId);
    switch (validationResult) {
      case "USER_NOT_FOUND":
        throw new CustomException("USER#1_001", "구독 대상 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
      case "DUPLICATE_SUBSCRIPTION":
        throw new CustomException("SUBSCRIPTION#2_001", "이미 구독 중입니다.", HttpStatus.CONFLICT);
      default:
        break;
    }

    Subscription subscription = Subscription.builder()
        .userId(userId)
        .subUserId(subUserId)
        .build();

    return SubscriptionCreateResponse.from(subscriptionRepository.save(subscription));
  }

  @Transactional
  public void deleteSubscription(Long userId, Long subUserId) {
    Subscription subscription = subscriptionRepository.findSubscription(userId, subUserId)
        .orElseThrow(() -> new CustomException(
            "SUBSCRIPTION#3_001", "구독 관계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    subscriptionRepository.delete(subscription);
  }
}
