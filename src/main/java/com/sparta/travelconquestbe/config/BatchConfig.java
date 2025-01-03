package com.sparta.travelconquestbe.config;

import com.sparta.travelconquestbe.api.client.s3.service.S3Service;
import com.sparta.travelconquestbe.common.batch.Policy.MultiSkipPolicy;
import com.sparta.travelconquestbe.common.batch.itemreader.Tour1MultiResourceItemReader;
import com.sparta.travelconquestbe.common.batch.itemreader.Tour2MultiResourceItemReader;
import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final S3Service s3Service;
  private final BatchUtil batchUtil;

  @Bean
  public SkipPolicy multiSkipPolicy() {
    return new MultiSkipPolicy();
  }

  @Bean
  public TaskExecutor taskExecutor() {
    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    taskExecutor.setConcurrencyLimit(10); // 스레드 수를 설정
    return taskExecutor;
  }

  // Job 설정
  @Bean(name = "tour1Job")
  public Job tour1Job(JobRepository jobRepository, Step tour1Step) {
    return new JobBuilder("tour1Job", jobRepository).start(tour1Step).build();
  }

  // Step 설정
  @Bean
  @JobScope
  public Step tour1Step(
      JobRepository jobRepository,
      @Qualifier("tour1MultiResourceItemReader") ItemReader<FieldSet> reader,
      @Qualifier("tour1DataProcessor") ItemProcessor<FieldSet, LocationData> processor,
      ItemWriter<LocationData> writer,
      PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("tour1Step", jobRepository)
        .<FieldSet, LocationData>chunk(1000, platformTransactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skipPolicy(multiSkipPolicy())
        .transactionManager(platformTransactionManager)
        .build();
  }

  @Bean
  @Lazy
  public MultiResourceItemReader<FieldSet> tour1MultiResourceItemReader() {
    Tour1MultiResourceItemReader reader = new Tour1MultiResourceItemReader();
    reader.setResources(s3Service.getS3ResourcesAsTempFiles("CSV/Tour1"));
    return reader;
  }

  // Tour2 Job 설정
  @Bean(name = "tour2Job")
  public Job tour2Job(JobRepository jobRepository, Step tour2Step) {
    return new JobBuilder("tour2Job", jobRepository).start(tour2Step).build();
  }

  // Tour2 Step 설정
  @Bean
  @JobScope
  public Step tour2Step(
      JobRepository jobRepository,
      @Qualifier("tour2MultiResourceItemReader") ItemReader<LocationData> reader,
      ItemWriter<LocationData> writer,
      PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("tour2Step", jobRepository)
        .<LocationData, LocationData>chunk(1000, platformTransactionManager)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skipPolicy(multiSkipPolicy())
        .transactionManager(platformTransactionManager)
        .build();
  }

  @Bean
  @Lazy
  public MultiResourceItemReader<LocationData> tour2MultiResourceItemReader() {
    Tour2MultiResourceItemReader reader = new Tour2MultiResourceItemReader(batchUtil);
    reader.setResources(s3Service.getS3ResourcesAsTempFiles("CSV/Tour2"));
    return reader;
  }
}
