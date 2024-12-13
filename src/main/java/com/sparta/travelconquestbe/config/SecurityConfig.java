package com.sparta.travelconquestbe.config;

import com.sparta.travelconquestbe.common.config.filter.JwtAuthenticationFilter;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.sparta.travelconquestbe.api.auth.service.CustomOAuth2UserService;
import com.sparta.travelconquestbe.common.handler.CustomOAuth2AuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtHelper jwtHelper;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF 설정 비활성화 (람다 방식)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users/signup", "/api/users/login/**", "/api/users/oauth/**",
                "/api/users/additional-info", "/login.html", "/app.js", "/api/admins/**", "/signup.html")
            .permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .successHandler(customOAuth2AuthenticationSuccessHandler)
            .failureUrl("/api/users/login?error=true")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtHelper),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
