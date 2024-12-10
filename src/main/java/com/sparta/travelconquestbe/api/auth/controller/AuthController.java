package com.sparta.travelconquestbe.api.auth.controller;

import com.sparta.travelconquestbe.api.auth.dto.request.AuthLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.api.auth.service.AuthService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<Void> signUp(@Valid @RequestBody AuthSignUpRequest request) {
    String token = authService.signUp(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody AuthLoginRequest request) {
    String token = authService.login(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
  }

  @GetMapping("/login/kakao")
  public ResponseEntity<Void> kakaoLoginRedirect() {
    String kakaoLoginUrl = authService.createKakaoLoginUrl();
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(kakaoLoginUrl)).build();
  }

  @GetMapping("/oauth/kakao/callback")
  public ResponseEntity<?> kakaoLoginCallback(@RequestParam("code") String code) {
    try {
      String jwtToken = authService.handleKakaoLogin(code);
      return ResponseEntity.ok()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
          .body("로그인 성공" + jwtToken);
    } catch (CustomException e) {
      if (e.getHttpStatus() == HttpStatus.FOUND) {
        // 추가 정보 입력 페이지로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, e.getErrorMessage())
            .build();
      }
      return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }
  }

  @PostMapping("/additional-info")
  public ResponseEntity<Map<String, String>> handleAdditionalInfo(@RequestBody SignUpAdditionalInfoRequest request) {
    try {
      String jwtToken = authService.saveAdditionalInfo(request);

      Map<String, String> responseBody = new HashMap<>();
      responseBody.put("message", "회원 가입 및 로그인 성공");
      responseBody.put("jwtToken", jwtToken);

      return ResponseEntity.ok()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
          .body(responseBody);
    } catch (CustomException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }
  }

  @GetMapping("/additional-info")
  public String showAdditionalInfoPage() {
    return "additional-info";
  }
}