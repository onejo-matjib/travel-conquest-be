package com.sparta.travelconquestbe.domain.partyMember.repository;

import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

}