package com.sparta.travelconquestbe.domain.partyMember.repository;

import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.enums.MemberType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

  Optional<PartyMember> findByUserIdAndPartyIdAndMemberType(Long id, Long id1,
      MemberType memberType);
}