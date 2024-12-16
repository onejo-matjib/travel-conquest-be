package com.sparta.travelconquestbe.api.user.service;

import com.sparta.travelconquestbe.api.user.dto.respones.UserResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.sparta.travelconquestbe.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserResponse getUserInfo(Long id, AuthUserInfo user){
    User targetUser = userRepository.findById(id)
        .orElseThrow(() -> new CustomException("USER#3_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (targetUser.getType() == UserType.ADMIN && user.getType() != UserType.ADMIN) {
      throw new CustomException("USER#2_001", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    return UserResponse.builder()
        .id(targetUser.getId())
        .name(targetUser.getName())
        .nickname(targetUser.getNickname())
        .email(targetUser.getEmail())
        .birth(targetUser.getBirth())
        .title(targetUser.getTitle().name())
        .subscriptionsCount(targetUser.getSubscriptionsCount())
        .build();
  }

}
