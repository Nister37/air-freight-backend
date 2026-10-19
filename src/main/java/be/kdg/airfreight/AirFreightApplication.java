package be.kdg.airfreight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirFreightApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirFreightApplication.class, args);
    }
}
