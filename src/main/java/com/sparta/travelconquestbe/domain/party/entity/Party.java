package com.sparta.travelconquestbe.domain.party.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMembers;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parties")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party extends TimeStampCreateUpdate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private int count;

  @Column(nullable = false)
  private int countMax;

  @Column(nullable = false)
  private PartyStatus status;

  @Column(nullable = false)
  private boolean passwordStatus;

  private String password;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "party", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<PartyMembers> partyMembers = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "party", cascade = CascadeType.REMOVE)
  private List<Tag> tags = new ArrayList<>();
}