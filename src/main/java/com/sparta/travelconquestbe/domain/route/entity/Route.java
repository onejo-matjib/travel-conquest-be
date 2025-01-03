package com.sparta.travelconquestbe.domain.route.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreateUpdate;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.bookmark.entity.Bookmark;
import com.sparta.travelconquestbe.domain.review.entity.Review;
import com.sparta.travelconquestbe.domain.route.enums.RouteStatus;
import com.sparta.travelconquestbe.domain.routelocation.entity.RouteLocation;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "status = 'AUTHORIZED'")
public class Route extends TimeStampCreateUpdate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @ColumnDefault("0")
  private Long totalDistance;

  @ColumnDefault("0")
  private int money;

  @ColumnDefault("0")
  private String estimatedTime;

  @Setter
  @Enumerated(EnumType.STRING)
  private RouteStatus status;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "route",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  @BatchSize(size = 100)
  private final List<RouteLocation> locations = new ArrayList<>();

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "route",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  @BatchSize(size = 100)
  private final List<Review> reviews = new ArrayList<>();

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "route",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  @BatchSize(size = 100)
  private final List<Bookmark> bookmarks = new ArrayList<>();

  public void validCreatorOrAdmin(Long userId, UserType type) {
    if (!Objects.equals(userId, this.getUser().getId()) && type != UserType.ADMIN) {
      throw new CustomException("ROUTE#4_001", "본인의 루트 혹은 관리자만 사용할 수 있습니다.", HttpStatus.FORBIDDEN);
    }
  }
}
