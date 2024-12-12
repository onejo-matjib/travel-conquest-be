package com.sparta.travelconquestbe.api.auth.controller;

import com.sparta.travelconquestbe.api.auth.dto.info.UserInfo;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.api.auth.dto.respones.KakaoLoginResult;
import com.sparta.travelconquestbe.api.auth.service.AuthService;
import com.sparta.travelconquestbe.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
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
  public ResponseEntity<?> kakaoLoginCallback(@RequestParam("code") String code, HttpServletRequest request) {

    KakaoLoginResult result = authService.handleKakaoLogin(code);

    if(result.isNewUser()) {
      request.getSession().setAttribute("tempUserInfo", result.getUserInfo());
      return ResponseEntity.status(HttpStatus.FOUND)
          .header(HttpHeaders.LOCATION, "/api/users/additional-info")
          .build();
    } else {
      return ResponseEntity.ok()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.getToken())
          .body("로그인 성공 " + result.getToken());
    }
  }

  @PostMapping("/additional-info")
  public ResponseEntity<Map<String, String>> handleAdditionalInfo(@RequestBody SignUpAdditionalInfoRequest request, HttpServletRequest httpRequest) {

    // 세션에서 tempUserInfo 가져오기
    UserInfo tempUserInfo = (UserInfo) httpRequest.getSession().getAttribute("tempUserInfo");

    if (tempUserInfo == null) {
      throw new CustomException("AUTH#1_033", "임시 사용자 정보가 없습니다.", HttpStatus.NOT_FOUND);
    }

    try {
      String jwtToken = authService.saveAdditionalInfo(request, tempUserInfo);

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