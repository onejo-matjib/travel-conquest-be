package com.sparta.travelconquestbe.api.user.dto.respones;

import com.sparta.travelconquestbe.domain.user.enums.Title;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRankingResponse {
  private Long id;
  private String nickname;
  private int subscriptionCount;
  private Title title;

  public UserRankingResponse(Long id, String nickname, int subscriptionCount, Title title) {
    this.id = id;
    this.nickname = nickname;
    this.subscriptionCount = subscriptionCount;
    this.title = title;
  }
}
