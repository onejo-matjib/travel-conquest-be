package com.sparta.travelconquestbe.common.batch.trigger;

import com.sparta.travelconquestbe.common.annotation.AdminUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trigger")
@EnableAsync
public class BatchJobController {
  private final JobLauncher jobLauncher;

  private final Job tour1Job;

  private final Job tour2Job;

  @AdminUser
  @PostMapping("/csvinit")
  public ResponseEntity<String> triggerBatch() throws Exception {
    log.info("CSV 배치작업 시작");
    triggerJobsConcurrently();
    log.info("CSV 배치작업 종료");
    return ResponseEntity.ok("Job completed successfully");
  }

  @Async
  public void triggerJobsConcurrently()
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {
    jobLauncher.run(tour1Job, new JobParameters());
    jobLauncher.run(tour2Job, new JobParameters());
  }
}
