package com.sparta.travelconquestbe.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.api.auth.dto.info.KakaoUserInfo;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthLoginRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.AuthSignUpRequest;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

  // 임시로 저장할 KakaoUserInfo
  private KakaoUserInfo tempKakaoUserInfo;

  public String signUp(AuthSignUpRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new CustomException("USER_005", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
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
    return jwtHelper.createToken(user.getId(), user.getEmail(), user.getType(), user.getProviderType());
  }

  public String login (AuthLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException("USER_006", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("AUTH_030", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user.getId(), user.getEmail(), user.getType(), user.getProviderType());
  }

  public String createKakaoLoginUrl() {
    return "https://kauth.kakao.com/oauth/authorize"
        + "?client_id=" + clientId
        + "&redirect_uri=" + redirectUri
        + "&response_type=code";
  }

  public String handleKakaoLogin(String code) {
    KakaoUserInfo kakaoUserInfo = getKakaoUserInfoFromCode(code);
    Optional<User> existingUser = userRepository.findByProviderId(kakaoUserInfo.getId());

    if (existingUser.isPresent()) {
      User user = existingUser.get();
      return jwtHelper.createToken(user.getId(), user.getEmail(), user.getType(), user.getProviderType());
    }

    tempKakaoUserInfo = kakaoUserInfo;
    throw new CustomException("AUTH_012", "/api/users/additional-info", HttpStatus.FOUND);
  }

  private KakaoUserInfo getKakaoUserInfoFromCode(String code) {
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

  private KakaoUserInfo getKakaoUserInfo(String accessToken) {
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
      throw new CustomException("AUTH_013", "액세스 토큰 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  private KakaoUserInfo parseUserInfo(String responseBody) {
    try {
      JsonNode jsonNode = objectMapper.readTree(responseBody);
      Long id = jsonNode.get("id").asLong();
      String email = jsonNode.path("kakao_account").path("email").asText(null);
      String nickname = jsonNode.path("properties").path("nickname").asText();
      return new KakaoUserInfo(id, email, nickname);
    } catch (Exception e) {
      logger.error("사용자 정보 파싱 실패: {}", e.getMessage());
      throw new CustomException("AUTH_014", "사용자 정보 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  public String saveAdditionalInfo(SignUpAdditionalInfoRequest request) {
    if (tempKakaoUserInfo == null) {
      throw new CustomException("AUTH_016", "임시 사용자 정보가 없습니다.", HttpStatus.BAD_REQUEST);
    }

    User newUser = User.builder()
        .name(request.getName())
        .birth(request.getBirth())
        .email(tempKakaoUserInfo.getEmail())
        .nickname(tempKakaoUserInfo.getNickname())
        .password("")
        .providerId(tempKakaoUserInfo.getId())
        .providerType("KAKAO")
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .build();

    User savedUser = userRepository.save(newUser);

    return jwtHelper.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getType(), savedUser.getProviderType());
  }
}

