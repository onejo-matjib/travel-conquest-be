package com.sparta.travelconquestbe.api.user.controller;

import com.sparta.travelconquestbe.api.user.dto.respones.UserResponse;
import com.sparta.travelconquestbe.api.user.service.UserService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUser(@PathVariable Long id, @AuthUser AuthUserInfo user) {
    UserResponse userResponse = userService.getUserInfo(id, user);
    return ResponseEntity.status(HttpStatus.OK).body(userResponse);
  }

  @PutMapping()
  public ResponseEntity<Void> deleteUser(@AuthUser AuthUserInfo userInfo) {
    userService.deleteUser(userInfo);
    return ResponseEntity.noContent().build();
  }
}
