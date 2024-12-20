package com.sparta.travelconquestbe.domain.report.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.domain.report.entity.QReport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryQueryDslImpl implements ReportRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ReportSearchResponse> findAllReports(Pageable pageable) {
    QReport report = QReport.report;

    List<ReportSearchResponse> results = queryFactory
        .select(Projections.constructor(ReportSearchResponse.class,
            report.id,
            report.reporterId.id,
            report.targetId.id,
            report.reportCategory,
            report.reason,
            report.status,
            report.createdAt,
            report.checkedAt,
            report.adminId
        ))
        .from(report)
        .where(report.checkedAt.isNull().or(
            report.checkedAt.eq(
                queryFactory.select(report.checkedAt.max())
                    .from(report)
                    .where(report.targetId.eq(report.targetId))
            )
        ))
        .orderBy(report.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = queryFactory
        .selectFrom(report)
        .where(report.checkedAt.isNull().or(
            report.checkedAt.eq(
                queryFactory.select(report.checkedAt.max())
                    .from(report)
                    .where(report.targetId.eq(report.targetId))
            )
        ))
        .fetchCount();

    return new PageImpl<>(results, pageable, total);
  }
}