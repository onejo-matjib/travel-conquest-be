package com.sparta.travelconquestbe.domain.party.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.domain.PartyTag.entity.PartyTag;
import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private int count;

  @Column(nullable = false)
  private int countMax;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PartyStatus status;

  @Column(nullable = false)
  private boolean passwordStatus;

  private String password;

  @OneToMany(mappedBy = "party", cascade = CascadeType.REMOVE, orphanRemoval = true)
  @Builder.Default // 기본값 설정
  private List<PartyMember> partyMember = new ArrayList<>();

  @OneToMany(mappedBy = "party", cascade = CascadeType.REMOVE, orphanRemoval = true)
  @Builder.Default // 기본값 설정
  private List<PartyTag> partyTags = new ArrayList<>();
}