//package com.sparta.travelconquestbe.api.party.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
//import com.sparta.travelconquestbe.domain.party.entity.Party;
//import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
//import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
//import com.sparta.travelconquestbe.domain.user.entity.User;
//import com.sparta.travelconquestbe.domain.user.enums.Title;
//import com.sparta.travelconquestbe.domain.user.enums.UserType;
//import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//public class PartyServiceTest {
//
//  @Autowired
//  private PartyService partyService;
//
//  @Autowired
//  private PartyRepository partyRepository;
//
//  @Autowired
//  private UserRepository userRepository;
//
//  private Party testParty;
//
//  private List<User> testUsers;
//
//  int success;
//  int fail;
//
//  @BeforeEach
//  void setup() {
//    userRepository.deleteAll();
//    partyRepository.deleteAll();
//    success = 0;
//    fail = 0;
//
//    testUsers = IntStream.rangeClosed(1, 10000)
//        .mapToObj(i -> User.builder()
//            .name("User" + i)
//            .nickname("Nickname" + i)
//            .email("user" + i + "@test.com")
//            .password("password")
//            .birth("2000-01-01")
//            .type(UserType.AUTHENTICATED_USER)
//            .title(Title.CONQUEROR)
//            .subscriptionCount(1)
//            .build())
//        .collect(Collectors.toList());
//    userRepository.saveAll(testUsers);
//
//    // 테스트 파티 생성
//    Party party = Party.builder()
//        .leaderNickname("testLeader")
//        .name("Test Party")
//        .description("A party for testing")
//        .count(0)
//        .countMax(30)
//        .status(PartyStatus.OPEN)
//        .passwordStatus(false)
//        .build();
//    testParty = partyRepository.save(party);
//  }
//
//  @Test
//  @Transactional
//  void concurrentJoinParty() throws InterruptedException {
//    int threadCount = 10000; // 동시에 참가 시도할 스레드 수
//    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//    CountDownLatch latch = new CountDownLatch(threadCount);
//
//    for (int i = 0; i < threadCount; i++) {
//      User currentUser = testUsers.get(i % testUsers.size());
//      AuthUserInfo userInfo = new AuthUserInfo(
//          currentUser.getId(),
//          currentUser.getName(),
//          currentUser.getNickname(),
//          currentUser.getEmail(),
//          currentUser.getPassword(),
//          currentUser.getBirth(),
//          currentUser.getType(),
//          currentUser.getTitle()
//      );
//
//      executorService.execute(() -> {
//        try {
//          partyService.joinParty(userInfo, testParty.getId());
//          success = success + 1;
//        } catch (Exception e) {
//          // 참가 실패한 경우 예외 로그 출력
//          System.out.println("파티 참여 실패: " + e.getMessage());
//          fail = fail + 1;
//        } finally {
//          System.out.println("Success : " + success);
//          System.out.println("fail : " + fail);
//          latch.countDown();
//        }
//      });
//    }
//
//    latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기
//    executorService.shutdown();
//
//    // 파티 상태 확인
//    Party updatedParty = partyRepository.findById(testParty.getId()).orElseThrow();
//    assertThat(updatedParty.getCount()).isEqualTo(30); // 최대 인원 제한 확인
//    assertThat(updatedParty.getStatus()).isEqualTo(PartyStatus.FULL); // 상태가 FULL로 변경되었는지 확인
//  }
//}
