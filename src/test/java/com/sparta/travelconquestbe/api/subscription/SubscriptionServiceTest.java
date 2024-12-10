package com.sparta.travelconquestbe.api.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.service.SubscriptionService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SubscriptionServiceTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SubscriptionService subscriptionService;

  private Long userId;
  private Long subUserId;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userId = 1L;
    subUserId = 2L;
  }

  @Test
  @DisplayName("구독 생성 성공")
  void createSubscriptionSuccess() {
    when(subscriptionRepository.validateSubscriptionCreation(userId, subUserId)).thenReturn(
        "VALID");
    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(userRepository.findById(subUserId)).thenReturn(Optional.of(new User()));
    when(subscriptionRepository.save(any())).thenReturn(new Subscription());

    SubscriptionCreateResponse response = subscriptionService.createSubscription(userId, subUserId);

    assertNotNull(response);
    verify(subscriptionRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("구독 생성 실패 - 본인 구독")
  void createSubscriptionFailure_SelfSubscribe() {
    CustomException exception = assertThrows(CustomException.class,
        () -> subscriptionService.createSubscription(userId, userId));

    assertEquals("SUBSCRIPTION_001", exception.getErrorCode());
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  @DisplayName("구독 생성 실패 - 대상 없음")
  void createSubscriptionFailure_NoTargetUser() {
    when(subscriptionRepository.validateSubscriptionCreation(userId, subUserId)).thenReturn(
        "USER_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class,
        () -> subscriptionService.createSubscription(userId, subUserId));

    assertEquals("USER_001", exception.getErrorCode());
    verify(subscriptionRepository, never()).save(any());
  }
}
