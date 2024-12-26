package com.sparta.travelconquestbe.domain.partyMember.repository;

import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

  Optional<PartyMember> findByUserIdAndPartyId(Long userId, Long partyId);
}