package com.sparta.travelconquestbe.domain.party.repository;

import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface  PartyRepositoryQueryDsl {
  Page<PartySearchResponse> searchAllPartise(Pageable pageable);
}
