package com.sparta.travelconquestbe.api.user.service;

import com.sparta.travelconquestbe.api.user.dto.respones.UserRankingResponse;
import com.sparta.travelconquestbe.api.user.dto.respones.UserResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.sparta.travelconquestbe.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

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
        .subscriptionsCount(targetUser.getSubscriptionCount())
        .build();
  }

  @Transactional
  public void deleteUser(AuthUserInfo userInfo) {
    User user = userRepository.findById(userInfo.getId())
        .orElseThrow(() -> new CustomException("USER#3_002", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (user.getDeletedAt() != null) {
      throw new CustomException("USER#4_001", "이미 탈퇴한 유저입니다.", HttpStatus.CONFLICT);
    }

    String deletedNickname = "delete_" + user.getNickname();
    user.changeNickname(deletedNickname);
    user.delete();
    userRepository.save(user);
  }

  // 캐싱
  @Cacheable(value = "topUsersCache", key = "'top100Users'")
  public List<UserRankingResponse> getTop100UsersBySubscriptions() {
    return userRepository.findTop100UsersBySubscriptions(PageRequest.of(0, 100))
        .stream()
        .map(user -> UserRankingResponse.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .subscriptionCount(user.getSubscriptionCount())
            .title(user.getTitle())
            .build())
        .toList();
  }

  @Transactional
  public void updateTitlesForEligibleUsers() {
    List<User> usersToUpdate = userRepository.findUsersToUpdateTitle();
    usersToUpdate.forEach(user -> user.changeTitle(Title.CONQUEROR));
    userRepository.saveAll(usersToUpdate);
  }

}
