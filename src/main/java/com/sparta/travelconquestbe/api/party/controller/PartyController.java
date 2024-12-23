package com.sparta.travelconquestbe.api.party.controller;

import com.sparta.travelconquestbe.api.party.dto.PartyCreateReseponse;
import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.service.PartyService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

  private final PartyService partyService;

  @PostMapping
  public ResponseEntity<PartyCreateReseponse> createParty(
      @AuthUser AuthUserInfo userInfo,
      @RequestBody PartyCreateRequest request
  ) {
    PartyCreateReseponse reseponse = partyService.createParty(userInfo, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(reseponse);
  }
}