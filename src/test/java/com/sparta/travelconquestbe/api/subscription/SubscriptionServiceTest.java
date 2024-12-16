package com.sparta.travelconquestbe.api.subscription;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionListResponse;
import com.sparta.travelconquestbe.api.subscription.service.SubscriptionService;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.subscription.entity.Subscription;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

class SubscriptionServiceTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SubscriptionService subscriptionService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("구독 생성 성공")
  void createSubscription_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long subUserId = 2L;
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.validateSubscriptionCreation(user.getId(), subUserId)).thenReturn(
        "VALID");
    Subscription saved = Subscription.builder().id(1L).userId(user.getId()).subUserId(subUserId)
        .build();
    when(subscriptionRepository.save(any(Subscription.class))).thenReturn(saved);

    SubscriptionCreateResponse response = subscriptionService.createSubscription(user, subUserId);

    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals(subUserId, response.getSubUserId());
  }

  @Test
  @DisplayName("구독 생성 실패 - 자기 구독")
  void createSubscription_SelfSubscription() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(user, user.getId());
    });

    assertEquals("SUBSCRIPTION#1_001", exception.getErrorCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }

  @Test
  @DisplayName("구독 생성 실패 - 구독 대상 없음")
  void createSubscription_TargetUserNotFound() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long subUserId = 2L;
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.validateSubscriptionCreation(user.getId(), subUserId)).thenReturn(
        "USER_NOT_FOUND");

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(user, subUserId);
    });

    assertEquals("SUBSCRIPTION#3_001", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("구독 생성 실패 - 중복 구독")
  void createSubscription_DuplicateSubscription() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long subUserId = 2L;
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.validateSubscriptionCreation(user.getId(), subUserId)).thenReturn(
        "DUPLICATE_SUBSCRIPTION");

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.createSubscription(user, subUserId);
    });

    assertEquals("SUBSCRIPTION#2_001", exception.getErrorCode());
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
  }

  @Test
  @DisplayName("구독 삭제 성공")
  void deleteSubscription_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long subUserId = 2L;
    Subscription subscription = Subscription.builder().id(1L).userId(user.getId())
        .subUserId(subUserId).build();
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.findSubscription(user.getId(), subUserId)).thenReturn(
        Optional.of(subscription));

    assertDoesNotThrow(() -> subscriptionService.deleteSubscription(user, subUserId));
    verify(subscriptionRepository).delete(subscription);
  }

  @Test
  @DisplayName("구독 삭제 실패 - 구독 관계 없음")
  void deleteSubscription_NotFound() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    Long subUserId = 2L;
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.findSubscription(user.getId(), subUserId)).thenReturn(
        Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.deleteSubscription(user, subUserId);
    });

    assertEquals("SUBSCRIPTION#3_002", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("구독 목록 조회 성공")
  void searchFollowings_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    PageRequest pageable = PageRequest.of(0, 10);
    Subscription s = Subscription.builder().id(1L).userId(user.getId()).subUserId(2L).build();
    Page<Subscription> page = new PageImpl<>(List.of(s), pageable, 1);

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.findAllByUserId(user.getId(), pageable)).thenReturn(page);

    SubscriptionListResponse response = subscriptionService.searchFollowings(user, pageable);
    assertNotNull(response);
    assertEquals(1, response.getTotalFollowings());
    assertEquals(1, response.getFollowings().size());
  }

  @Test
  @DisplayName("구독 목록 비어있음")
  void searchFollowings_EmptyResult() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    PageRequest pageable = PageRequest.of(0, 10);
    Page<Subscription> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.findAllByUserId(user.getId(), pageable)).thenReturn(emptyPage);

    SubscriptionListResponse response = subscriptionService.searchFollowings(user, pageable);
    assertNotNull(response);
    assertEquals(0, response.getTotalFollowings());
  }

  @Test
  @DisplayName("구독 삭제 실패 - 같은 유저")
  void deleteSubscription_SameUserAndTarget() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());

    CustomException exception = assertThrows(CustomException.class, () -> {
      subscriptionService.deleteSubscription(user, user.getId());
    });

    assertEquals("SUBSCRIPTION#3_002", exception.getErrorCode());
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
  }

  @Test
  @DisplayName("내 구독자 목록 조회 성공")
  void searchMyFollowers_Success() {
    AuthUserInfo user = new AuthUserInfo(1L, "", "", "", "", "", UserType.USER, Title.TRAVELER);
    PageRequest pageable = PageRequest.of(0, 10);
    Subscription s1 = Subscription.builder().id(1L).userId(2L).subUserId(user.getId()).build();
    Subscription s2 = Subscription.builder().id(2L).userId(3L).subUserId(user.getId()).build();
    Page<Subscription> mockPage = new PageImpl<>(List.of(s1, s2), pageable, 2);

    when(userRepository.getReferenceById(user.getId())).thenReturn(
        User.builder().id(user.getId()).build());
    when(subscriptionRepository.findAllBySubUserId(user.getId(), pageable)).thenReturn(mockPage);

    SubscriptionListResponse response = subscriptionService.searchFollowers(user, pageable);
    assertNotNull(response);
    assertEquals(2, response.getTotalFollowings());
    assertEquals(2, response.getFollowings().size());
  }
}
