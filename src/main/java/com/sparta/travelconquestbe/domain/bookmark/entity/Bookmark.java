package com.sparta.travelconquestbe.domain.bookmark.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "route_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark extends TimeStampCreated {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_id", nullable = false)
  private Route route;

  public static Bookmark createBookmark(User user, Route route, boolean isDuplicate) {
    if (isDuplicate) {
      throw new CustomException("BOOKMARK_001", "이미 등록된 즐겨찾기입니다.", HttpStatus.CONFLICT);
    }
    return Bookmark.builder()
        .user(user)
        .route(route)
        .build();
  }
}
