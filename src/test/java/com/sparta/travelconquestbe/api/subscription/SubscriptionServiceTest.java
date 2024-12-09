package com.sparta.travelconquestbe.api.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

class SubscriptionServiceTest {

  @InjectMocks
  private SubscriptionService subscriptionService;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private UserRepository userRepository;

  private final Long userId = 1L;
  private final Long subUserId = 2L;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("구독_성공")
  void createSubscription_Success() {
    Long userId = 1L;
    Long subUserId = 2L;

    // Mock User 객체를 빌더 패턴으로 생성
    User subUser = User.builder()
        .id(subUserId)
        .name("Test User")
        .nickname("TestNickname")
        .email("test@example.com")
        .password("password")
        .providerId(100L)
        .providerType("LOCAL")
        .birth("1990-01-01")
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .build();

    when(userRepository.findById(subUserId)).thenReturn(Optional.of(subUser));
    when(subscriptionRepository.isSubscribed(userId, subUserId)).thenReturn(false);
    when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
      Subscription subscription = invocation.getArgument(0);
      return Subscription.builder()
          .id(1L)
          .userId(subscription.getUserId())
          .subUserId(subscription.getSubUserId())
          .build();
    });

    SubscriptionCreateResponse response = subscriptionService.createSubscription(userId, subUserId);

    assertNotNull(response);
    assertEquals(subUserId, response.getSubUserId());
    verify(userRepository, times(1)).findById(subUserId);
    verify(subscriptionRepository, times(1)).isSubscribed(userId, subUserId);
    verify(subscriptionRepository, times(1)).save(any(Subscription.class));
  }


  @Test
  @DisplayName("구독_실패_본인_구독_시도")
  void createSubscription_Fail_SelfSubscription() {
    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, userId);
    });

    assertEquals("SUBSCRIPTION_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    verify(userRepository, never()).findById(anyLong());
    verify(subscriptionRepository, never()).isSubscribed(anyLong(), anyLong());
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  @DisplayName("구독_실패_구독_대상_없음")
  void createSubscription_Fail_SubUserNotFound() {
    when(userRepository.findById(subUserId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, subUserId);
    });

    assertEquals("USER_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    verify(userRepository, times(1)).findById(subUserId);
    verify(subscriptionRepository, never()).isSubscribed(anyLong(), anyLong());
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  @DisplayName("구독_실패_이미_구독_중")
  void createSubscription_Fail_AlreadySubscribed() {
    User subUser = User.builder().id(subUserId).build();

    when(userRepository.findById(subUserId)).thenReturn(Optional.of(subUser));
    when(subscriptionRepository.isSubscribed(userId, subUserId)).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(userId, subUserId);
    });

    assertEquals("SUBSCRIPTION_002", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    verify(userRepository, times(1)).findById(subUserId);
    verify(subscriptionRepository, times(1)).isSubscribed(userId, subUserId);
    verify(subscriptionRepository, never()).save(any());
  }
}
