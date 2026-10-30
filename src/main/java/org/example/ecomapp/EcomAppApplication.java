package org.example.ecomapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
@SpringBootApplication
public class EcomAppApplication {

    public static void main(String[] args) {
        // Load environment variables from .env

        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .load();

        // Make the variables available to Spring Boot
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        // Load expiry minutes with a fallback default of 15 if not specified in .env
       //String expiryMinutes = dotenv.get("ORDER_RESERVATION_EXPIRY_MINUTES", "15");
       //System.setProperty("ORDER_RESERVATION_EXPIRY_MINUTES", expiryMinutes);

        SpringApplication.run(EcomAppApplication.class, args);
    }

}
