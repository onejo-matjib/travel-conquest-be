package com.sparta.travelconquestbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TravelConquestBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelConquestBeApplication.class, args);
    }

}
