package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

  @Query(value =
      "SELECT EXISTS(" +
          "SELECT 1 FROM reports " +
          "WHERE reporter_id = :reporterId " +
          "AND target_id = :targetId " +
          "AND report_category = :reportCategory)",
      nativeQuery = true)
  boolean isDuplicateReport(Long reporterId, Long targetId, String reportCategory);

  @Query(value =
      "SELECT r.status " +
          "FROM reports r " +
          "WHERE r.target_id = :targetId " +
          "ORDER BY r.id DESC " +
          "LIMIT 1",
      nativeQuery = true)
  Optional<Villain> findLatestStatus(Long targetId);

  @Query("SELECT new com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse(" +
      "r.id, r.reporterId.id, r.targetId.id, r.reportCategory, r.reason, " +
      "r.status, r.createdAt, r.checkedAt, r.adminId) " +
      "FROM Report r ORDER BY r.id DESC")
  Page<ReportSearchResponse> findAllReports(Pageable pageable);
}
