package com.sparta.travelconquestbe;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest
public abstract class TestContainerSupport {

  private static final String REDIS_IMAGE = "redis:7.0";
  private static final int REDIS_PORT = 6379;
  private static final GenericContainer<?> REDIS;
  private static final String MYSQL_IMAGE = "mysql:8.0";
  private static final int MYSQL_PORT = 3306;
  private static final JdbcDatabaseContainer<?> MYSQL;

  static {
    REDIS =
        new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
            .withExposedPorts(REDIS_PORT)
            .withNetworkMode("dev_network")
            .withReuse(true);
    REDIS.start();
    MYSQL =
        new MySQLContainer<>(MYSQL_IMAGE)
            .withExposedPorts(MYSQL_PORT)
            .withNetworkMode("dev_network")
            .withReuse(true);
    MYSQL.start();
  }

  @DynamicPropertySource
  public static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(REDIS_PORT)));
  }
}
