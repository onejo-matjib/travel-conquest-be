package com.sparta.travelconquestbe.domain.user.repository;

import com.sparta.travelconquestbe.domain.user.entity.User;
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
public class UserBulkRepository {

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public void saveAll(List<User> users) {
    jdbcTemplate.batchUpdate(
        "INSERT INTO users (name, nickname, email, password, type, title, subscription_count, birth, created_at, updated_at) "
            +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            User user = users.get(i);
            ps.setString(1, user.getName());
            ps.setString(2, user.getNickname());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getType().name());
            ps.setString(6, user.getTitle() != null ? user.getTitle().name() : null);
            ps.setInt(7, user.getSubscriptionCount());
            ps.setString(8, user.getBirth() != null ? user.getBirth().toString() : null);
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, java.sql.Timestamp.valueOf(LocalDateTime.now()));
          }

          @Override
          public int getBatchSize() {
            return users.size();
          }
        }
    );
  }
}
