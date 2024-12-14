package com.sparta.travelconquestbe.domain.report.entity;

import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "reporter_id", nullable = false)
  private Long reporterId;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "report_category", nullable = false)
  private ReportCategory reportCategory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Reason reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Villain status;

  @Column
  private LocalDateTime checkedAt;
}
