package com.sparta.travelconquestbe.api.auth.service;

import com.sparta.travelconquestbe.api.auth.dto.info.UserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String providerType = userRequest.getClientRegistration().getRegistrationId();
    String providerId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    System.out.println("email: " + email);

    // 사용자 정보 확인
    if (providerId == null || email == null) {
      throw new CustomException("AUTH#3_003", "사용자 정보 로드 실패", HttpStatus.NOT_FOUND);
    }

    UserInfo.builder()
        .id(providerId)
        .email(email)
        .nickname(name)
        .providerType(providerType)
        .build();

    return oAuth2User;
  }
}

