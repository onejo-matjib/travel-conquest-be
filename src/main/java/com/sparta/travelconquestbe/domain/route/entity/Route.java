package com.sparta.travelconquestbe.domain.route.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends TimeStampCreateUpdate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  private Long totalDistance;

  private int money;

  private String estimatedTime;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "route",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  private List<RouteLocation> locations = new ArrayList<>();
}
