package com.sparta.travelconquestbe.common.auth;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class JwtAuthentication extends AbstractAuthenticationToken {

  private final Claims claims;

  public JwtAuthentication(Claims claims) {
    super(Collections.singletonList(new SimpleGrantedAuthority("USER")));
    this.claims = claims;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return claims.get("userId"); // 사용자 ID를 principal로 사용
  }

  public Claims getClaims() {
    return claims;
  }
}
