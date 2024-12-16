package com.sparta.travelconquestbe.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.api.auth.dto.info.UserInfo;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.common.exception.ErrorResponse;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtHelper jwtHelper;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2User oAuth2User = oauthToken.getPrincipal();

    String providerType = oauthToken.getAuthorizedClientRegistrationId();
    String providerId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");

    Optional<User> optionalUser = userRepository.findByEmail(email);

    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      if (user.getDeletedAt() != null) {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType("text/plain; charset=UTF-8");
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("AUTH#4_004")
            .errorMessage("탈퇴한 유저입니다.")
            .httpStatus(HttpStatus.CONFLICT.value())
            .timestamp(System.currentTimeMillis())
            .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return;
      }
      String token = jwtHelper.createToken(user);

      response.setHeader("Authorization", "Bearer " + token);
      response.setContentType("text/plain; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write("로그인 성공!!! 토큰 발급 : " + token);
      return;
    } else {
      request.getSession().setAttribute("tempUserInfo", UserInfo.builder()
          .id(providerId)
          .email(email)
          .nickname(oAuth2User.getAttribute("name"))
          .build());
    }
    response.sendRedirect("/api/users/additional-info");
  }

}
