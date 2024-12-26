package com.sparta.travelconquestbe.domain.PartyTag.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "partyTags")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyTag extends TimeStampCreated {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "party_id")
  private Party party;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id")
  private Tag tag;
}