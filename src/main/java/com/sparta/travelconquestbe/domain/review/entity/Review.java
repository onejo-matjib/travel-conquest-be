package com.sparta.travelconquestbe.domain.review.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
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
  @JoinColumn(name = "route_id")
  private Route route;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  public static Review createReview(int rating, String comment, Route route, User user) {
    return Review.builder()
        .rating(rating)
        .comment(comment)
        .route(route)
        .user(user)
        .build();
  }
}
