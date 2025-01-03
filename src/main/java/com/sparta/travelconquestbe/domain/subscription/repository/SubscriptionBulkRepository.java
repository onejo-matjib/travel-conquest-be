package com.sparta.travelconquestbe.domain.subscription.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SubscriptionBulkRepository {

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public void saveAll(List<Long> subUserIds) {

    jdbcTemplate.batchUpdate(
        "INSERT INTO subscriptions (user_id, sub_user_id, created_at) VALUES (?, ?, ?)",
        new BatchPreparedStatementSetter() {

          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, 2L);
            ps.setLong(2, subUserIds.get(i));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
          }

          @Override
          public int getBatchSize() {
            return subUserIds.size();
          }
        }
    );
  }
}
