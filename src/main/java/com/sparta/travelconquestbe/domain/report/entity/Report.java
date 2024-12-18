package com.sparta.travelconquestbe.domain.report.entity;

import com.sparta.travelconquestbe.common.annotation.ValidEnum;
import com.sparta.travelconquestbe.common.entity.TimeStampCreated;
import com.sparta.travelconquestbe.domain.report.enums.Reason;
import com.sparta.travelconquestbe.domain.report.enums.ReportCategory;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Report extends TimeStampCreated {

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
  @ValidEnum(enumClass = ReportCategory.class, message = "유효하지 않은 카테고리입니다")
  @Column(name = "report_category", nullable = false)
  private ReportCategory reportCategory;

  @Enumerated(EnumType.STRING)
  @ValidEnum(enumClass = Reason.class, message = "유효하지 않은 신고 사유 입니다.")
  @Column(nullable = false)
  private Reason reason;

  @Enumerated(EnumType.STRING)
  @ValidEnum(enumClass = Villain.class, message = "유효하지 않은 빌런 상태 입니다")
  @Column(nullable = false)
  private Villain status;

  @Column
  private LocalDateTime checkedAt;

  @Column
  private Long adminId;

  public void markProcessed(Long adminId) {
    this.checkedAt = LocalDateTime.now();
    this.adminId = adminId;
  }
}
