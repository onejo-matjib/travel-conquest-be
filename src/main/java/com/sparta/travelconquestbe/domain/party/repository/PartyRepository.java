package com.sparta.travelconquestbe.domain.party.repository;

import com.sparta.travelconquestbe.api.party.dto.response.PartySearchResponse;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.enums.PartySort;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PartyRepository extends JpaRepository<Party, Long>, PartyRepositoryQueryDsl {

  Page<PartySearchResponse> searchAllPartise(Pageable pageable, PartySort partySort,
      String direction);

  @Query("SELECT p.id FROM Party p")
  List<Long> findAllPartyId();
}