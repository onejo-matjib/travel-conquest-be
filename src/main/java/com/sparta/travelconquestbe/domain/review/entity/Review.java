package com.sparta.travelconquestbe.domain.review.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.route.entity.Route;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "route_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends TimeStampCreated {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int rating;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @ManyToOne
  @JoinColumn(name = "route_id", nullable = false)
  private Route route;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public static Review createReview(int rating, String comment, Route route, User user) {
    return Review.builder()
        .rating(rating)
        .comment(comment)
        .route(route)
        .user(user)
        .build();
  }

  public void validateOwner(Long userId) {
    if (!this.user.getId().equals(userId)) {
      throw new CustomException("REVIEW#3_001", "본인의 리뷰만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
    }
  }
}
