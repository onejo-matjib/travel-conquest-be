package com.sparta.travelconquestbe.domain.party.repository;

import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartyRepositoryQueryDsl {

  Page<PartySearchResponse> searchAllPartise(Pageable pageable, PartySort partySort,
      String direction);
}