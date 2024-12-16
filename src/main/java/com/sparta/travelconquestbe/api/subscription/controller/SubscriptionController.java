package com.sparta.travelconquestbe.api.subscription.controller;

import com.sparta.travelconquestbe.api.subscription.dto.request.SubscriptionCreateRequest;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionListResponse;
import com.sparta.travelconquestbe.api.subscription.service.SubscriptionService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<SubscriptionCreateResponse> createSubscription(
      @AuthUser AuthUserInfo user,
      @Valid @RequestBody SubscriptionCreateRequest request
  ) {
    SubscriptionCreateResponse response = subscriptionService.createSubscription(user,
        request.getSubUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{subUserId}")
  public ResponseEntity<Void> deleteSubscription(
      @AuthUser AuthUserInfo user,
      @PathVariable Long subUserId
  ) {
    subscriptionService.deleteSubscription(user, subUserId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/followings")
  public ResponseEntity<SubscriptionListResponse> searchMyFollowings(
      @AuthUser AuthUserInfo user,
      Pageable pageable
  ) {
    SubscriptionListResponse response = subscriptionService.searchFollowings(user, pageable);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/followers")
  public ResponseEntity<SubscriptionListResponse> searchMyFollowers(
      @AuthUser AuthUserInfo user,
      Pageable pageable
  ) {
    SubscriptionListResponse response = subscriptionService.searchFollowers(user, pageable);
    return ResponseEntity.ok(response);
  }
}
