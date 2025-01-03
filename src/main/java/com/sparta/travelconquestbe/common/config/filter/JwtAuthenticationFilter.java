package com.sparta.travelconquestbe.common.config.filter;

import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.auth.JwtAuthentication;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtHelper jwtHelper;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      try {
        AuthUserInfo authUserInfo= jwtHelper.getAuthUserInfoFromToken(token);

        var userOptional = userRepository.findById(authUserInfo.getId());
        if (userOptional.isPresent()) {
          var user = userOptional.get();
          if (user.isSuspended()) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "정지된 사용자입니다. 정지 기간 : " + user.getSuspendedUntil());
            return;
          }
        } else {
          response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다.");
        }
        JwtAuthentication authentication = new JwtAuthentication(authUserInfo);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (CustomException e) {
        response.sendError(e.getHttpStatus().value(), e.getMessage());
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
