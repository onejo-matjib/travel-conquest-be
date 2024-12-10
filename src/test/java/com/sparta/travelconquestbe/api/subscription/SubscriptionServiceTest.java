package com.sparta.travelconquestbe.api.subscription;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.service.SubscriptionService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private SubscriptionService subscriptionService;

  @Test
  @DisplayName("구독 생성 - 성공")
  void createSubscription_Success() {
    Long userId = 1L;
    Long subUserId = 2L;

    when(subscriptionRepository.validateSubscriptionCreation(userId, subUserId)).thenReturn("VALID");

    Subscription savedSubscription = Subscription.builder()
        .id(1L)
        .userId(userId)
        .subUserId(subUserId)
        .build();
    when(subscriptionRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

    SubscriptionCreateResponse response = subscriptionService.createSubscription(userId, subUserId);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals(subUserId, response.getSubUserId());

    verify(subscriptionRepository, times(1)).validateSubscriptionCreation(userId, subUserId);
    verify(subscriptionRepository, times(1)).save(any(Subscription.class));
  }

  @Test
  @DisplayName("구독 생성 실패 - 자기 구독")
  void createSubscription_SelfSubscription() {
    Long userId = 1L;

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, userId);
    });

    assertEquals("SUBSCRIPTION#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    assertEquals("본인을 구독할 수 없습니다.", exception.getErrorMessage());

    verify(subscriptionRepository, never()).validateSubscriptionCreation(anyLong(), anyLong());
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  @DisplayName("구독 생성 실패 - 구독 대상 없음")
  void createSubscription_TargetUserNotFound() {
    Long userId = 1L;
    Long subUserId = 2L;

    when(subscriptionRepository.validateSubscriptionCreation(userId, subUserId)).thenReturn("USER_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, subUserId);
    });

    assertEquals("USER#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("구독 대상 사용자를 찾을 수 없습니다.", exception.getErrorMessage());

    verify(subscriptionRepository, times(1)).validateSubscriptionCreation(userId, subUserId);
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  @DisplayName("구독 생성 실패 - 중복 구독")
  void createSubscription_DuplicateSubscription() {
    Long userId = 1L;
    Long subUserId = 2L;

    when(subscriptionRepository.validateSubscriptionCreation(userId, subUserId)).thenReturn("DUPLICATE_SUBSCRIPTION");

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, subUserId);
    });

    assertEquals("SUBSCRIPTION#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    assertEquals("이미 구독 중입니다.", exception.getErrorMessage());

    verify(subscriptionRepository, times(1)).validateSubscriptionCreation(userId, subUserId);
    verify(subscriptionRepository, never()).save(any());
  }
}
