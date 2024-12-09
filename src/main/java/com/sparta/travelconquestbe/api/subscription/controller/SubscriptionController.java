package com.sparta.travelconquestbe.api.subscription.controller;

import com.sparta.travelconquestbe.api.subscription.dto.request.SubscriptionCreateRequest;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.service.SubscriptionService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<SubscriptionCreateResponse> createSubscription(
      @AuthUser Long userId,
      @Valid @RequestBody SubscriptionCreateRequest request) {
    SubscriptionCreateResponse response = subscriptionService.createSubscription(userId,
        request.getSubUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
