package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

  @Query(value =
      "SELECT EXISTS("
          + "SELECT 1 FROM reports "
          + "WHERE reporter_id = :reporterId "
          + "AND target_id = :targetId "
          + "AND report_category = :reportCategory)",
      nativeQuery = true)
  boolean isDuplicateReport(
      @Param("reporterId") Long reporterId,
      @Param("targetId") Long targetId,
      @Param("reportCategory") String reportCategory);

  @Query(value =
      "SELECT r.status "
          + "FROM reports r "
          + "WHERE r.target_id = :targetId "
          + "ORDER BY r.id DESC "
          + "LIMIT 1",
      nativeQuery = true)
  Optional<Villain> findLatestStatus(@Param("targetId") Long targetId);
}
