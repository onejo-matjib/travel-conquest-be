//package com.sparta.travelconquestbe.api.user;
//
//import com.sparta.travelconquestbe.domain.user.entity.User;
//import com.sparta.travelconquestbe.domain.user.enums.Title;
//import com.sparta.travelconquestbe.domain.user.enums.UserType;
//import com.sparta.travelconquestbe.domain.user.repository.UserBulkRepository;
//import java.util.ArrayList;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//class UserBulkInsertTest {
//
//  @Autowired
//  private UserBulkRepository userBulkRepository;
//
//  @Test
//  @Rollback(false)
//  @Transactional
//  @DisplayName("더미 유저 대량 생성")
//  void generateFakeUsers() {
//    long start = System.currentTimeMillis();
//
//    List<User> users = new ArrayList<>();
//    for (long i = 1; i < 1000002; i++) {
//      User user = User.builder()
//          .name("User" + i)
//          .nickname("Nickname" + i)
//          .email("user" + i + "@example.com")
//          .password("password" + i)
//          .type(UserType.USER)
//          .title(Title.TRAVELER)
//          .birth("1990-01-01")
//          .subscriptionCount(0)
//          .build();
//
//      users.add(user);
//
//      if (users.size() == 50000) {
//        userBulkRepository.saveAll(users);
//        users.clear();
//      }
//    }
//    if (!users.isEmpty()) {
//      userBulkRepository.saveAll(users);
//    }
//
//    long end = System.currentTimeMillis();
//    System.out.println("총 소요 시간(ms): " + (end - start));
//  }
//}
