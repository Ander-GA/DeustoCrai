package es.deusto.spq.deustocrai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeustoCraiAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeustoCraiAppApplication.class, args);
    }

}