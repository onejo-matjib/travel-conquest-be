package com.sparta.travelconquestbe.api.subscription;

import com.sparta.travelconquestbe.TestContainerSupport;
import com.sparta.travelconquestbe.domain.subscription.repository.SubscriptionBulkRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Rollback(false)
@Transactional
@DisplayName("구독 데이터 대량 삽입")
class SubscriptionBulkInsertTest extends TestContainerSupport {

  @Autowired
  private SubscriptionBulkRepository subscriptionBulkRepository;

  @Test
  void generateSubscriptions() {
    long start = System.currentTimeMillis();

    List<Long> subUserIds = new ArrayList<>();
    for (long i = 3; i <= 1000002; i++) {
      subUserIds.add(i);
      if (subUserIds.size() >= 50000) {
        subscriptionBulkRepository.saveAll(subUserIds);
        subUserIds.clear();
      }
    }
    if (!subUserIds.isEmpty()) {
      subscriptionBulkRepository.saveAll(subUserIds);
      subUserIds.clear();
    }

    long end = System.currentTimeMillis();
    System.out.println("총 소요 시간(ms): " + (end - start));
  }
}
