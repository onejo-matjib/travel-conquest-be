package com.sparta.travelconquestbe;

import com.sparta.travelconquestbe.common.exception.CustomException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;

@EnableJpaAuditing
@SpringBootApplication
public class TravelConquestBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelConquestBeApplication.class, args);
    }

}
