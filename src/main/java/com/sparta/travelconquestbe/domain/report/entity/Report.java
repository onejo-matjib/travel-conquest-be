package com.sparta.travelconquestbe.domain.report.entity;

import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.persistence.*;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporterId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  private User targetId;

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
