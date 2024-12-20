package com.sparta.travelconquestbe.domain.report.repository;

import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryQueryDsl {
  Page<ReportSearchResponse> findAllReports(Pageable pageable);
}