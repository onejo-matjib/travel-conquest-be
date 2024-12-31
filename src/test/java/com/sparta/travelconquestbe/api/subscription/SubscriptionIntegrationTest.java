package com.sparta.travelconquestbe.api.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.api.subscription.dto.request.SubscriptionCreateRequest;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionListResponse;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback(false)
@Testcontainers
class SubscriptionIntegrationTest {

  // MySQL 컨테이너 정의
  @Container
  private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("testdb")
      .withUsername("testuser")
      .withPassword("testpass");

  // 스프링부트 프로퍼티를 Testcontainers 컨테이너 값으로 동적 주입
  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int port;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private EntityManager entityManager;

  private String tokenA;
  private String tokenB;

  @BeforeEach
  void setUp() throws Exception {
    // 기존에 해당 이메일이 있으면 삭제 (매번 테스트를 깨끗하게 시작)
    userRepository.findByEmail("subTestA@example.com").ifPresent(userRepository::delete);
    userRepository.findByEmail("subTestB@example.com").ifPresent(userRepository::delete);

    // 1) 유저 A 회원가입 + 로그인
    var signUpA = """
            {
              "email": "subTestA@example.com",
              "password": "aaaa1111",
              "name": "A유저",
              "birth": "19950101",
              "nickname": "userA"
            }
        """;
    mockMvc.perform(post("/api/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(signUpA))
        .andExpect(status().isCreated());

    var loginA = """
            {
              "email": "subTestA@example.com",
              "password": "aaaa1111"
            }
        """;
    var loginAResult = mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginA))
        .andExpect(status().isOk())
        .andReturn();
    tokenA = loginAResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);

    // 2) 유저 B 회원가입 + 로그인
    var signUpB = """
            {
              "email": "subTestB@example.com",
              "password": "bbbb2222",
              "name": "B유저",
              "birth": "19960101",
              "nickname": "userB"
            }
        """;
    mockMvc.perform(post("/api/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(signUpB))
        .andExpect(status().isCreated());

    var loginB = """
            {
              "email": "subTestB@example.com",
              "password": "bbbb2222"
            }
        """;
    var loginBResult = mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginB))
        .andExpect(status().isOk())
        .andReturn();
    tokenB = loginBResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
  }

  @Test
  @DisplayName("구독 - A유저가 B유저를 구독, 목록 조회 -> 구독 취소 -> 재확인")
  void subscriptionIntegrationTest() throws Exception {
    // (A) A유저 가져오기
    User userA = userRepository.findByEmail("subTestA@example.com")
        .orElseThrow(() -> new IllegalStateException("A유저가 DB에서 안 보임"));
    Long userAId = userA.getId();

    // (B) B유저 가져오기
    User userB = userRepository.findByEmail("subTestB@example.com")
        .orElseThrow(() -> new IllegalStateException("B유저가 DB에서 안 보임"));
    Long subUserId = userB.getId();

    // (C) A -> B 구독
    var req = SubscriptionCreateRequest.builder()
        .subUserId(subUserId)
        .build();

    mockMvc.perform(post("/api/subscriptions")
            .header(HttpHeaders.AUTHORIZATION, tokenA)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated());

    // (D) A의 구독 목록 조회
    var followingListJson = mockMvc.perform(get("/api/subscriptions/followings?page=1&limit=10")
            .header(HttpHeaders.AUTHORIZATION, tokenA))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    SubscriptionListResponse followingsRes =
        objectMapper.readValue(followingListJson, SubscriptionListResponse.class);

    assertThat(followingsRes.getFollowings()).hasSize(1);
    assertThat(followingsRes.getFollowings().get(0).getSubUserId()).isEqualTo(subUserId);

    // (E) 구독 취소
    mockMvc.perform(delete("/api/subscriptions/" + subUserId)
            .header(HttpHeaders.AUTHORIZATION, tokenA))
        .andExpect(status().isNoContent());

    // (F) DB 동기화 + 1차 캐시 초기화
    entityManager.flush();
    entityManager.clear();

    assertThat(subscriptionRepository.findAll()).isEmpty();

    // (G) 다시 조회해서 확인
    var afterDelJson = mockMvc.perform(get("/api/subscriptions/followings?page=1&limit=10")
            .header(HttpHeaders.AUTHORIZATION, tokenA))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    SubscriptionListResponse afterDelRes =
        objectMapper.readValue(afterDelJson, SubscriptionListResponse.class);

    assertThat(afterDelRes.getFollowings()).isEmpty();
  }
}
