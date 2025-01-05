package com.sparta.travelconquestbe.api.subscription.service;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionListResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;

  @Transactional
  @CacheEvict(value = "followingsCache", allEntries = true)
  public SubscriptionCreateResponse createSubscription(AuthUserInfo user, Long subUserId) {
    User referenceUser = userRepository.getReferenceById(user.getId());

    if (referenceUser.getId().equals(subUserId)) {
      throw new CustomException("SUBSCRIPTION#1_001", "본인을 구독할 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    String validationResult = subscriptionRepository.validateSubscriptionCreation(
        referenceUser.getId(), subUserId);
    switch (validationResult) {
      case "USER_NOT_FOUND":
        throw new CustomException("SUBSCRIPTION#3_001", "구독 대상 사용자가 존재하지 않습니다.",
            HttpStatus.NOT_FOUND);
      case "DUPLICATE_SUBSCRIPTION":
        throw new CustomException("SUBSCRIPTION#2_001", "이미 구독 중입니다.", HttpStatus.CONFLICT);
      default:
        break;
    }

    Subscription subscription = Subscription.builder()
        .userId(referenceUser.getId())
        .subUserId(subUserId)
        .build();

    User subUser = userRepository.findById(subUserId)
        .orElseThrow(() -> new CustomException("SUBSCRIPTION#3_003", "구독 대상 사용자가 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));

    subUser.updateSubscriptionCount(+1);
    userRepository.save(subUser);

    return SubscriptionCreateResponse.from(subscriptionRepository.save(subscription));
  }

  @Transactional
  @CacheEvict(value = {
      "followingsCache",
      "followersCache"
  }, allEntries = true)
  public void deleteSubscription(AuthUserInfo user, Long subUserId) {
    User referenceUser = userRepository.getReferenceById(user.getId());

    Subscription subscription = subscriptionRepository.findSubscription(referenceUser.getId(),
            subUserId)
        .orElseThrow(
            () -> new CustomException("SUBSCRIPTION#3_002", "구독 관계를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND)
        );
    subscriptionRepository.delete(subscription);

    User subUser = userRepository.findById(subUserId)
        .orElseThrow(() -> new CustomException("SUBSCRIPTION#3_004", "구독 대상 사용자가 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));

    subUser.updateSubscriptionCount(-1);
    userRepository.save(subUser);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "followingsCache", key = "#user.id + '_' + #page + '_' + #limit")
  public SubscriptionListResponse searchFollowings(AuthUserInfo user, int page, int limit) {
    User referenceUser = userRepository.getReferenceById(user.getId());
    PageRequest pageRequest = PageRequest.of(page - 1, limit);
    Page<Subscription> subscriptions = subscriptionRepository.findAllByUserId(referenceUser.getId(),
        pageRequest);
    Long totalFollowings = subscriptions.getTotalElements();

    return SubscriptionListResponse.from(subscriptions, totalFollowings);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "followersCache", key = "#user.id + '_' + #page + '_' + #limit")
  public SubscriptionListResponse searchFollowers(AuthUserInfo user, int page, int limit) {
    User referenceUser = userRepository.getReferenceById(user.getId());
    PageRequest pageRequest = PageRequest.of(page - 1, limit);
    Page<Subscription> subscriptions = subscriptionRepository.findAllBySubUserId(
        referenceUser.getId(), pageRequest);
    Long totalFollowers = subscriptions.getTotalElements();

    return SubscriptionListResponse.from(subscriptions, totalFollowers);
  }
}
