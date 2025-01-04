package com.sparta.travelconquestbe.api.partymember.controller;

import com.sparta.travelconquestbe.api.partymember.service.PartyMemberService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/party-members")
@RequiredArgsConstructor
@Validated
public class PartyMemberController {

  private final PartyMemberService partyMemberService;

  @DeleteMapping("/{partyId}")
  public ResponseEntity<Void> deleteParty(
      @AuthUser AuthUserInfo userInfo,
      @PathVariable Long partyId
  ) {
    partyMemberService.partyLeave(userInfo, partyId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}