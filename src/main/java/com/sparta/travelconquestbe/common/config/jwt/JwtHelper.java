package com.sparta.travelconquestbe.common.config.jwt;

import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.entity.User;
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

  @Value("${JWT_SECRET_KEY}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private Long expiration;

  // JWT 토큰 생성 (일반 로그인 및 소셜 로그인 공통 사용)
  public String createToken(User user) {
    Claims claims = Jwts.claims().setSubject(user.getEmail());
    claims.put("id", user.getId());
    claims.put("name", user.getName());
    claims.put("nickname", user.getNickname());
    claims.put("providerType", user.getProviderType());
    claims.put("birth", user.getBirth());
    claims.put("userType", user.getType());
    claims.put("title", user.getTitle());

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
      throw new CustomException("AUTH#1_003", "인증 토큰이 만료되었습니다. 다시 로그인 해주세요.", HttpStatus.UNAUTHORIZED);
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
      throw new CustomException("AUTH#1_004", "유효하지 않은 인증 토큰입니다.", HttpStatus.UNAUTHORIZED);
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
      throw new CustomException("AUTH#1_005", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
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
