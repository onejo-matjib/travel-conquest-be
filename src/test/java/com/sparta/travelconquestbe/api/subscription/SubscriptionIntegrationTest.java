package com.sparta.travelconquestbe.api.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.travelconquestbe.TestContainerSupport;
import com.sparta.travelconquestbe.api.subscription.dto.request.SubscriptionCreateRequest;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionCreateResponse;
import com.sparta.travelconquestbe.api.subscription.dto.response.SubscriptionListResponse;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubscriptionIntegrationTest extends TestContainerSupport {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtHelper jwtHelper;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;
  private String user1Token;
  private String user2Token;
  private Long user1Id;
  private Long user2Id;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    userRepository.deleteAll();

    User user1 = User.builder()
        .email("user1@test.com")
        .password("test1234")
        .name("UserOne")
        .birth("19900101")
        .nickname("유저1")
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .providerType("LOCAL")
        .build();
    user1 = userRepository.save(user1);
    user1Id = user1.getId();
    user1Token = "Bearer " + jwtHelper.createToken(user1);

    User user2 = User.builder()
        .email("user2@test.com")
        .password("test1234")
        .name("UserTwo")
        .birth("19900202")
        .nickname("유저2")
        .type(UserType.USER)
        .title(Title.TRAVELER)
        .providerType("LOCAL")
        .build();
    user2 = userRepository.save(user2);
    user2Id = user2.getId();
    user2Token = "Bearer " + jwtHelper.createToken(user2);
  }

  @Nested
  @DisplayName("1. 구독 생성 테스트")
  class CreateSubscriptionTest {

    @Test
    @DisplayName("정상적으로 구독 생성 시 201 (CREATED)")
    void testCreateSubscription() throws Exception {
      SubscriptionCreateRequest request = new SubscriptionCreateRequest(user2Id);

      mockMvc.perform(post("/api/subscriptions")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(result -> {
            SubscriptionCreateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SubscriptionCreateResponse.class
            );
            assertThat(response.getSubUserId()).isEqualTo(user2Id);
          });
    }

    @Test
    @DisplayName("본인을 구독하려 하면 400 (BAD_REQUEST)")
    void testCreateSubscriptionSelf() throws Exception {
      SubscriptionCreateRequest request = new SubscriptionCreateRequest(user1Id);

      mockMvc.perform(post("/api/subscriptions")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 구독 중인 경우 409 (CONFLICT)")
    void testCreateDuplicateSubscription() throws Exception {
      SubscriptionCreateRequest request = new SubscriptionCreateRequest(user2Id);

      mockMvc.perform(post("/api/subscriptions")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      mockMvc.perform(post("/api/subscriptions")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("2. 구독 목록 조회 테스트")
  class SubscriptionListTest {

    @BeforeEach
    void initData() throws Exception {
      SubscriptionCreateRequest request = new SubscriptionCreateRequest(user2Id);

      mockMvc.perform(post("/api/subscriptions")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("User1의 구독 목록 조회")
    void testSearchFollowings() throws Exception {
      mockMvc.perform(get("/api/subscriptions/followings")
              .header(HttpHeaders.AUTHORIZATION, user1Token)
              .param("page", "1")
              .param("limit", "10"))
          .andExpect(status().isOk())
          .andExpect(result -> {
            SubscriptionListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SubscriptionListResponse.class
            );
            assertThat(response.getTotalFollowings()).isEqualTo(1L);
            assertThat(response.getFollowings().get(0).getSubUserId()).isEqualTo(user2Id);
          });
    }
  }
}
