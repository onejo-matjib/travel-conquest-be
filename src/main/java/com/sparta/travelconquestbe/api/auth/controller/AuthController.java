package com.sparta.travelconquestbe.api.auth.controller;

import com.sparta.travelconquestbe.api.auth.dto.info.KakaoUserInfo;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.api.auth.service.AuthService;
import com.sparta.travelconquestbe.common.annotation.AuthUser;
import com.sparta.travelconquestbe.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  @GetMapping("/login/kakao")
  public ResponseEntity<Void> kakaoLoginRedirect() {
    String kakaoLoginUrl = authService.createKakaoLoginUrl();
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(kakaoLoginUrl)).build();
  }

  @GetMapping("/kakao/callback")
  public ResponseEntity<?> kakaoLoginCallback(@RequestParam String code) {
    try {
      String jwtToken = authService.handleKakaoLogin(code);
      return ResponseEntity.ok()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
          .body(jwtToken);

    } catch (CustomException e) {
      logger.error("카카오 로그인 처리 중 예외 발생: 에러 코드 - {}, 메시지 - {}", e.getErrorCode(), e.getMessage());
      return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    } catch (Exception e) {
      logger.error("카카오 로그인 과정에서 예기치 않은 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 과정에서 예기치 않은 오류가 발생했습니다.");
    }
  }

  @PostMapping("/oauth/kakao/additional-info")
  public ResponseEntity<String> handleAdditionalInfo(@AuthUser Long userId, @RequestBody SignUpAdditionalInfoRequest request) {
    try {
      String jwtToken = authService.saveAdditionalInfo(userId, request);
      return ResponseEntity.ok()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
          .body("회원 가입 및 로그인 성공" + jwtToken);
    } catch (CustomException e) {
      logger.error("추가 정보 처리 중 예외 발생: 에러 코드 - {}, 메시지 - {}", e.getErrorCode(), e.getMessage());
      return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }
  }
}