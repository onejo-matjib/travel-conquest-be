

package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.TestContainerSupport;
import com.sparta.travelconquestbe.api.admin.dto.respones.AdminUpdateUserResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class AdminServiceTest extends TestContainerSupport {

  @Autowired
  private AdminService adminService;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void testBanUser() {
    User user = User.builder()
        .email("test@example.com")
        .password("password")
        .name("Test User")
        .nickname("testuser")
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .providerType("LOCAL")
        .birth("19900101")
        .build();
    userRepository.save(user);

    AuthUserInfo admin = new AuthUserInfo(
        1L, "Admin", "admin", "admin@example.com", "LOCAL",
        "19900101", UserType.ADMIN, Title.CONQUEROR
    );

    AdminUpdateUserResponse response = adminService.banUser(user.getId());

    User bannedUser = userRepository.findById(user.getId()).orElseThrow();
    Assertions.assertNotNull(bannedUser.getDeletedAt());
    Assertions.assertEquals("resign_testuser", bannedUser.getNickname());
    Assertions.assertEquals(user.getId(), response.getUserId());
    Assertions.assertEquals(UserType.USER, response.getUserType());
    Assertions.assertEquals("resign_testuser", response.getNickname());
  }
//
//  @Test
//  public void testUpdateUserLevel() {
//    User user = User.builder()
//        .email("test2@example.com")
//        .password("password")
//        .name("Test User2")
//        .nickname("testuser2")
//        .type(UserType.USER)
//        .title(Title.TRAVELER)
//        .providerType("LOCAL")
//        .birth("19900102")
//        .build();
//    userRepository.save(user);
//
//    AuthUserInfo admin = new AuthUserInfo(
//        1L, "Admin", "admin", "admin@example.com", "LOCAL",
//        "19900101", UserType.ADMIN, Title.CONQUEROR
//    );
//
//    AdminUpdateUserResponse response = adminService.updateUserLevel(admin, user.getId());
//
//    User upgradedUser = userRepository.findById(user.getId()).orElseThrow();
//    Assertions.assertEquals(UserType.AUTHENTICATED_USER, upgradedUser.getType());
//    Assertions.assertEquals(user.getId(), response.getUserId());
//    Assertions.assertEquals(UserType.AUTHENTICATED_USER, response.getUserType());
//  }
//
//  @Test
//  public void testUpdateAdminShouldFail() {
//    User adminUser = User.builder()
//        .email("admin2@example.com")
//        .password("password")
//        .name("Admin User")
//        .nickname("adminuser")
//        .type(UserType.ADMIN)
//        .title(Title.CONQUEROR)
//        .providerType("LOCAL")
//        .birth("19900103")
//        .build();
//    userRepository.save(adminUser);
//
//    AuthUserInfo admin = new AuthUserInfo(
//        1L, "Admin", "admin", "admin@example.com", "LOCAL",
//        "19900101", UserType.ADMIN, Title.CONQUEROR
//    );
//
//    CustomException exception = Assertions.assertThrows(CustomException.class, () -> {
//      adminService.updateUserLevel(admin, adminUser.getId());
//    });
//
//    Assertions.assertEquals("ADMIN#5_002", exception.getErrorCode());
//  }
}

