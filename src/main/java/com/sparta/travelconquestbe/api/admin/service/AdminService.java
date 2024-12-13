package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final JwtHelper jwtHelper;
  private final PasswordEncoder passwordEncoder;

  public void signUp(AuthSignUpRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new CustomException("ADMIN#4_001", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
    }

    User adminUser = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .birth(request.getBirth())
        .nickname(request.getNickname())
        .type(UserType.ADMIN)
        .title(Title.CONQUEROR)
        .providerType("LOCAL")
        .build();

    userRepository.save(adminUser);
  }

  public String login(AdminLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException("ADMIN#3_001", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND));

    if(!UserType.ADMIN.equals(user.getType())) {
      throw new CustomException("ADMIN#2_002", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("ADMIN#1_001", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user);
  }
}
