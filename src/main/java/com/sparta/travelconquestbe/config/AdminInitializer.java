package com.sparta.travelconquestbe.config;

import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {

    boolean adminExists = userRepository.findAll().stream()
        .anyMatch(user -> user.getType() == UserType.ADMIN);

    // 관리자 계정이 없다면 하나 생성
    if (!adminExists) {
      User adminUser = User.builder()
          .email("admin@admin.com")
          .password(passwordEncoder.encode("admin1234")) // passwordEncoder 사용
          .name("adminUser")
          .birth("19900101")
          .nickname("adminMaster")
          .type(UserType.ADMIN)
          .title(Title.CONQUEROR)
          .providerType("LOCAL")
          .build();
      userRepository.save(adminUser);
      System.out.println("초기 관리자 계정 생성 완료 : admin@admin.com / admin1234");
    }
  }
}