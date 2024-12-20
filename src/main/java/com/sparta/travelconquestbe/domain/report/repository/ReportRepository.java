package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryQueryDsl {

  // 중복 신고 확인
  @Query(value =
      "SELECT EXISTS(" +
          "SELECT 1 FROM reports " +
          "WHERE reporter_id = :reporterId " +
          "AND target_id = :targetId " +
          "AND report_category = :reportCategory)",
      nativeQuery = true)
  boolean isDuplicateReport(Long reporterId, Long targetId, String reportCategory);

  // 특정 사용자에 대한 가장 최신 신고의 상태를 조회
  @Query(value =
      "SELECT r.status " +
          "FROM reports r " +
          "WHERE r.target_id = :targetId " +
          "ORDER BY r.id DESC " +
          "LIMIT 1",
      nativeQuery = true)
  Optional<Villain> findLatestStatus(Long targetId);

}
