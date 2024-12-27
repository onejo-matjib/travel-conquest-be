//package com.sparta.travelconquestbe.api.party.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
//import com.sparta.travelconquestbe.domain.party.entity.Party;
//import com.sparta.travelconquestbe.domain.party.enums.PartyStatus;
//import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
//import com.sparta.travelconquestbe.domain.partyMember.repository.PartyMemberRepository;
//import com.sparta.travelconquestbe.domain.user.entity.User;
//import com.sparta.travelconquestbe.domain.user.enums.Title;
//import com.sparta.travelconquestbe.domain.user.enums.UserType;
//import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers
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
//
//  @Autowired
//  private RedisTemplate<String, String> redisTemplate;
//
//  private Party testParty;
//
//  private List<User> testUsers;
//
//  private static final String PARTY_COUNT_KEY_PREFIX = "party_count:";
//  @Autowired
//  private PartyMemberRepository partyMemberRepository;
//
//  @BeforeEach
//  void setup() {
//    partyMemberRepository.deleteAll(); // 의존성 이슈로 인한 순서배치
//    partyRepository.deleteAll();
//    userRepository.deleteAll();
//
//    assert userRepository.count() == 0 : "User 데이터베이스가 비어 있지 않습니다.";
//    assert partyRepository.count() == 0 : "Party 데이터베이스가 비어 있지 않습니다.";
//    assert partyMemberRepository.count() == 0 : "Party_member 데이터베이스가 비어 있지 않습니다.";
//
//    // User 엔터티 생성
//    testUsers = IntStream.rangeClosed(1, 10000)
//        .mapToObj(i -> User.builder()
//            .name("User" + i)
//            .nickname("Nickname" + i)
//            .email("user" + i + "@test.com") // 이메일 고유화
//            .password("password")
//            .birth("2000-01-01")
//            .type(UserType.AUTHENTICATED_USER)
//            .title(Title.CONQUEROR)
//            .subscriptionCount(1)
//            .build())
//        .collect(Collectors.toList());
//    userRepository.saveAll(testUsers);
//
//    // 파티 엔터티 생성
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
//  @AfterEach
//  void afterSet() {
//    partyMemberRepository.deleteAll();
//    partyRepository.deleteAll();
//    userRepository.deleteAll();
//  }
//
//  @Test
//  void verifyRedisLock() {
//    String lockKey = "partyId:" + testParty.getId();
//    String lockValue = "testLock";
//
//    redisTemplate.opsForValue().set(lockKey, lockValue, 1, TimeUnit.SECONDS);
//
//    assertThat(redisTemplate.opsForValue().get(lockKey)).isEqualTo(lockValue);
//  }
//
//  @Test
//  void concurrentJoinPartyWithRedisLock() throws InterruptedException {
//    int threadCount = 10000; // 동시에 참가 시도할 스레드 수
//    ExecutorService executorService = Executors.newFixedThreadPool(100); // 스레드 풀 크기 조정
//    CountDownLatch latch = new CountDownLatch(threadCount);
//
//    String redisKey = PARTY_COUNT_KEY_PREFIX + testParty.getId();
//    // Redis 초기화
//    redisTemplate.opsForValue().set(redisKey, String.valueOf(testParty.getCount()));
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
//          System.out.println("참여 성공!");
//        } catch (Exception e) {
//          // 예외 발생 시 출력
//          System.out.println("참여 실패: " + e.getMessage());
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
//    // Redis와 DB 상태 확인
//    Party updatedParty = partyRepository.findById(testParty.getId()).orElseThrow();
//    String redisCount = redisTemplate.opsForValue().get(redisKey);
//
//    System.out.println("Redis 인원: " + redisCount);
//    System.out.println("DB 인원: " + updatedParty.getCount());
//    System.out.println("방 상태: " + updatedParty.getStatus());
//
//    // Redis와 DB의 동기화 상태 검증
//    assertThat(redisCount).isEqualTo("30");
//    assertThat(updatedParty.getCount()).isEqualTo(30); // 최대 인원 제한 확인
//    assertThat(updatedParty.getStatus()).isEqualTo(PartyStatus.FULL); // 상태가 FULL로 변경되었는지 확인
//  }
//}
