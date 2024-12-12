package com.sparta.travelconquestbe.common.config.jwt;

import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtHelper {

  private String secretKey;

  @Value("${jwt.expiration}")
  private Long expiration;

  @PostConstruct
  protected void init() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
      keyGenerator.init(256);
      Key key = keyGenerator.generateKey();
      secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
    } catch (Exception e) {
      throw new RuntimeException("시크릿 키 생성 실패", e);
    }
  }

  // JWT 토큰 생성 (일반 로그인 및 소셜 로그인 공통 사용)
  public String createToken(Long userId, String email, UserType userType, String providerType) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put("userId", userId);
    claims.put("providerType", providerType);
    claims.put("userType", userType);

    Date now = new Date();
    Date validity = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (Exception e) {
      throw new CustomException("AUTH_010", "인증 토큰이 만료되었습니다. 다시 로그인 해주세요.", HttpStatus.UNAUTHORIZED);
    }
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody();
  }

  public Claims validateAndGetClaims(String token) {
    try {
      return Jwts.parser()
          .setSigningKey(secretKey)
          .parseClaimsJws(token)
          .getBody();
    } catch (Exception e) {
      throw new CustomException("AUTH_002", "유효하지 않은 인증 토큰입니다.", HttpStatus.UNAUTHORIZED);
    }
  }

  public AuthUserInfo getAuthUserInfoFromToken(String token) {
    try {
      Claims claims = getClaims(token);
      Long id = claims.get("id", Long.class);
      String name = claims.get("name", String.class);
      String nickname = claims.get("nickname", String.class);
      String email = claims.getSubject();
      String providerType = claims.get("providerType", String.class);
      String birth = claims.get("birth", String.class);
      String userTypeStr = claims.get("userType", String.class);
      UserType type = UserType.valueOf(userTypeStr);
      String titleStr = claims.get("title", String.class);
      Title title = Title.valueOf(titleStr);

      return new AuthUserInfo(id, name, nickname, email, providerType, birth, type, title);
    } catch (Exception e) {
      throw new CustomException("AUTH#1_001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
    }
  }

  public void storeRefreshToken(String userId, String refreshToken) {
    // Redis에 Refresh Token을 저장하는 로직
  }

  public Long getUserIdFromRefreshToken(String refreshToken) {
    // Redis에서 Refresh Token을 확인하고, 유효한 경우 사용자 ID 반환하는 로직
    return null; // 예시를 위한 null 반환
  }
}
