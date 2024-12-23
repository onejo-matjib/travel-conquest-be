package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.api.admin.dto.respones.ReportSearchResponse;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByReporterIdAndTargetIdAndReportCategory(Long reporterId, User targetId, ReportCategory reportCategory);

  List<Report> findAllByTargetIdAndCheckedAtIsNull(User targetId);

  @Query("SELECT new com.sparta.travelconquestbe.api.admin.dto.respones.ReportSearchResponse(" +
      "r.id, r.reporterId, r.targetId.id, r.reportCategory, r.reason, " +
      "r.checkedAt, r.adminId) " +
      "FROM Report r ORDER BY r.id DESC")
  Page<ReportSearchResponse> findAllReports(Pageable pageable);
}
