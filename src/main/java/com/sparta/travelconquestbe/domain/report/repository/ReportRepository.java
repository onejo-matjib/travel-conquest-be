package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

  @Query("SELECT r FROM Report r WHERE r.targetId = :targetId ORDER BY r.createdAt DESC")
  Optional<Report> findLatestReportByTargetId(@Param("targetId") Long targetId);

  @Query("SELECT r.status FROM Report r WHERE r.targetId = :targetId ORDER BY r.createdAt DESC")
  Optional<Villain> findTargetStatus(@Param("targetId") Long targetId);
}
