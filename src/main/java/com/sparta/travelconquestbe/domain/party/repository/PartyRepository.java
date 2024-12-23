package com.sparta.travelconquestbe.domain.party.repository;

import com.sparta.travelconquestbe.domain.party.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {

}