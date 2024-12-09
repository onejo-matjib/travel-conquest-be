package com.sparta.travelconquestbe.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.api.auth.dto.info.KakaoUserInfo;
import com.sparta.travelconquestbe.api.auth.dto.request.SignUpAdditionalInfoRequest;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final JwtHelper jwtHelper;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${kakao.client-id}")
  private String clientId;

  @Value("${kakao.redirect-uri}")
  private String redirectUri;

  @Value("${kakao.client-secret}")
  private String clientSecret;

  public ResponseEntity<?> handleKakaoLogin(String code) {
    KakaoUserInfo kakaoUserInfo = getKakaoUserInfoFromCode(code);
    Optional<User> existingUser = userRepository.findByProviderId(kakaoUserInfo.getId());

    if (existingUser.isPresent()) {
      return jwtHelper.createToken(existingUser.get().getId(), existingUser.get().getEmail(),
          existingUser.get().getProviderType());
    }

    if (kakaoUserInfo.getEmail() == null || kakaoUserInfo.getEmail().isEmpty()) {
      throw new CustomException("USER_006", "이메일 정보가 필요합니다.", HttpStatus.BAD_REQUEST);
    }

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create("/additional-info?kakaoId=" + kakaoUserInfo.getId()))
        .build();
  }

  public String createKakaoLoginUrl() {
    return "https://kauth.kakao.com/oauth/authorize"
        + "?client_id=" + clientId
        + "&redirect_uri=" + redirectUri
        + "&response_type=code";
  }

  public KakaoUserInfo getKakaoUserInfoFromCode(String code) {
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
      throw new CustomException("AUTH_007", "액세스 토큰 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
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
      throw new CustomException("AUTH_008", "사용자 정보 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  public String saveAdditionalInfo(Long userId, SignUpAdditionalInfoRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("USER_006", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    user.updateAdditionalInfo(request.getName(), request.getBirth());
    userRepository.save(user);

    return jwtHelper.createToken(user.getId(), user.getEmail(), user.getProviderType());
  }
}


