package com.sparta.travelconquestbe.common.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

public class JwtAuthentication extends AbstractAuthenticationToken {

  private final AuthUserInfo authUserInfo;

  public JwtAuthentication(AuthUserInfo authUserInfo) {
    super(Collections.singletonList(new SimpleGrantedAuthority("USER")));
    this.authUserInfo = authUserInfo;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return authUserInfo;
  }

  public AuthUserInfo getAuthUserInfo() {
    return authUserInfo;
  }
}
