package com.sparta.travelconquestbe.domain.partyMember.repository;

import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.enums.MemberType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

  Optional<PartyMember> findByUserIdAndPartyIdAndMemberType(Long userId, Long partyId,
      MemberType memberType);

  Optional<PartyMember> findByUserIdAndPartyId(Long userId, Long partyId);

  List<PartyMember> findByPartyIdAndMemberTypeNot(Long id, MemberType memberType);

  boolean existsByUserIdAndPartyId(Long userId, Long id);

  @Modifying
  @Query("DELETE FROM PartyMember pm WHERE pm.party.id = :partyId")
  void deletePartyMembersByPartyId(Long partyId);

  @Modifying
  @Query("DELETE FROM PartyMember pm WHERE pm.party.id = :partyId")
  void deleteByPartyId(Long partyId);
}