package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AdminService {

  private final UserRepository userRepository;
  private final JwtHelper jwtHelper;
  private final PasswordEncoder passwordEncoder;

  public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtHelper jwtHelper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtHelper = jwtHelper;
  }

  public void signUp(AuthSignUpRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new CustomException("USER#3_001", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .birth(request.getBirth())
        .nickname(request.getNickname())
        .type(UserType.ADMIN)
        .title(Title.CONQUEROR)
        .providerType("LOCAL")
        .build();

    userRepository.save(user);
  }

  public String login (AdminLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException("USER#1_037", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));

    System.out.println("비밀번호 : " + user.getPassword());
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("AUTH#2_032", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user.getId(), user.getEmail(), user.getType(), user.getProviderType());
  }
}
