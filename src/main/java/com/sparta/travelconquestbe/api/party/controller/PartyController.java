package com.sparta.travelconquestbe.api.party.controller;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.dto.request.PartyUpdateRequest;
import com.sparta.travelconquestbe.api.party.dto.response.PartyCreateResponse;
import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import com.sparta.travelconquestbe.api.party.dto.response.PartyUpdateResponse;
import com.sparta.travelconquestbe.api.party.service.PartyService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@Validated
public class PartyController {

  private final PartyService partyService;

  @PostMapping
  public ResponseEntity<PartyCreateResponse> createParty(
      @AuthUser AuthUserInfo userInfo,
      @Valid @RequestBody PartyCreateRequest request
  ) {
    PartyCreateResponse reseponse = partyService.createParty(userInfo, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(reseponse);
  }

  @GetMapping // 최신순 정렬
  public ResponseEntity<Page<PartySearchResponse>> searchAllPartise(
      @Positive @RequestParam(defaultValue = "1", value = "page") int page,
      @Positive @RequestParam(defaultValue = "10", value = "limit") int limit,
      @ValidEnum(enumClass = PartySort.class, message = "정렬할 컬럼값을 정확하게 입력해주세요.")
      @RequestParam(defaultValue = "CREATED_AT") String sort,
      @RequestParam(defaultValue = "DESC") String direction) {

    if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
      throw new CustomException("PARTY#1_002",
          "정렬 방향은 ASC 또는 DESC만 가능합니다.",
          HttpStatus.BAD_REQUEST);
    }
    Pageable pageable = PageRequest.of(
        page - 1,
        limit);

    PartySort partySort = PartySort.valueOf(sort.toUpperCase());

    return ResponseEntity.status(HttpStatus.OK)
        .body(partyService.searchAllPartise(pageable, partySort, direction));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PartyUpdateResponse> updateParty(
      @AuthUser AuthUserInfo userInfo,
      @PathVariable Long id,
      @Valid @RequestBody PartyUpdateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(partyService.updateParty(userInfo, id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteParty(
      @AuthUser AuthUserInfo userInfo,
      @PathVariable Long id
  ) {
    partyService.deleteParty(userInfo, id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}