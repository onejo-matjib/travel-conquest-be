package com.sparta.travelconquestbe.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.api.auth.dto.info.UserInfo;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.api.auth.dto.respones.KakaoLoginResult;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.sparta.travelconquestbe.domain.user.enums.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sparta.travelconquestbe.domain.user.enums.Title;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final JwtHelper jwtHelper;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${kakao.client-id}")
  private String clientId;

  @Value("${kakao.redirect-uri}")
  private String redirectUri;

  @Value("${kakao.client-secret}")
  private String clientSecret;

  public String signUp(AuthSignUpRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new CustomException("AUTH#4_001", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .birth(request.getBirth())
        .nickname(request.getNickname())
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .providerType("LOCAL")
        .build();

    userRepository.save(user);
    return jwtHelper.createToken(user);
  }

  public String login (AuthLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException("AUTH#3_002", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("AUTH#1_002", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user);
  }

  public String createKakaoLoginUrl() {
    return "https://kauth.kakao.com/oauth/authorize"
        + "?client_id=" + clientId
        + "&redirect_uri=" + redirectUri
        + "&response_type=code";
  }

  public KakaoLoginResult handleKakaoLogin(String code) {
    UserInfo kakaoUserInfo = getKakaoUserInfoFromCode(code);
    Optional<User> existingUser = userRepository.findByProviderId(kakaoUserInfo.getId());

    if (existingUser.isPresent()) {
      User user = existingUser.get();
      String jwtToken = jwtHelper.createToken(user);
      return KakaoLoginResult.existingUser(jwtToken);
    } else {
      kakaoUserInfo.saveProviderType("KAKAO");
      return KakaoLoginResult.newUser(kakaoUserInfo);
    }
  }

  private UserInfo getKakaoUserInfoFromCode(String code) {
    String accessToken = getKakaoAccessToken(code);
    return getKakaoUserInfo(accessToken);
  }

  private String getKakaoAccessToken(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String body = "grant_type=authorization_code"
        + "&client_id=" + clientId
        + "&redirect_uri=" + redirectUri
        + "&code=" + code
        + "&client_secret=" + clientSecret;

    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
    return parseAccessToken(response.getBody());
  }

  private UserInfo getKakaoUserInfo(String accessToken) {
    String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

    return parseUserInfo(response.getBody());
  }

  private String parseAccessToken(String responseBody) {
    try {
      JsonNode jsonNode = objectMapper.readTree(responseBody);
      return jsonNode.get("access_token").asText();
    } catch (Exception e) {
      logger.error("액세스 토큰 파싱 실패: {}", e.getMessage());
      throw new CustomException("AUTH#5_001", "액세스 토큰 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  private UserInfo parseUserInfo(String responseBody) {
    try {
      JsonNode jsonNode = objectMapper.readTree(responseBody);
      String id = jsonNode.get("id").toString();
      String email = jsonNode.path("kakao_account").path("email").asText(null);
      String nickname = jsonNode.path("properties").path("nickname").asText();
      return new UserInfo(id, email, nickname);
    } catch (Exception e) {
      logger.error("사용자 정보 파싱 실패: {}", e.getMessage());
      throw new CustomException("AUTH#5_002", "사용자 정보 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  // 소셜 로그인 신규 가입자 전용 + 추가 정보 입력받음
  public String saveAdditionalInfo(SignUpAdditionalInfoRequest request, UserInfo tempUserInfo) {
    if (tempUserInfo == null) {
      throw new CustomException("AUTH#5_003", "임시 사용자 정보가 없습니다.", HttpStatus.BAD_REQUEST);
    }

    User newUser = User.builder()
        .name(request.getName())
        .birth(request.getBirth())
        .email(tempUserInfo.getEmail())
        .nickname(tempUserInfo.getNickname())
        .password("")
        .providerId(tempUserInfo.getId())
        .providerType(tempUserInfo.getProviderType())
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .build();

    User savedUser = userRepository.save(newUser);

    return jwtHelper.createToken(savedUser);
  }

}

