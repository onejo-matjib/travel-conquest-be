package com.sparta.travelconquestbe.domain.user.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampAll;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends TimeStampAll {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String name;

  @Column(nullable = false, length = 30)
  private String nickname;

  @Column(nullable = false, unique = true, length = 50)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  private String providerId;
  private String providerType;

  @Column(nullable = false, length = 10)
  private String birth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserType type;

  @Enumerated(EnumType.STRING)
  private Title title;

  @Column(nullable = false)
  private int subscriptionCount = 0;

  public void changeNickname(String newNickname) {
    this.nickname = newNickname;
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  public void updateUserType() {
    if (this.type == UserType.USER) {
      this.type = UserType.AUTHENTICATED_USER;
      this.title = Title.PIONEER;
    } else {
      throw new CustomException("ADMIN#5_002", "등급 업그레이드가 불가능한 상태입니다.", HttpStatus.BAD_REQUEST);
    }
  }

  public void delete() {
    this.markDelete(LocalDateTime.now());
  }

  public void updateSubscriptionCount(int change) {
    int newCount = this.subscriptionCount + change;
    this.subscriptionCount = Math.max(newCount, 0);
  }
}
