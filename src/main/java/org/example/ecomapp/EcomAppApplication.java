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
        // Load .env when available (local development)
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();


        // Make the variables available to Spring Boot
        setPropertyIfMissing("DB_URL", dotenv.get("DB_URL"));
        setPropertyIfMissing("DB_USERNAME", dotenv.get("DB_USERNAME"));
        setPropertyIfMissing("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        //Load expiry minutes with a fallback default of 15 if not specified in .env
        //String expiryMinutes = dotenv.get("ORDER_RESERVATION_EXPIRY_MINUTES", "15");
        //setPropertyIfMissing("ORDER_RESERVATION_EXPIRY_MINUTES", expiryMinutes);

        SpringApplication.run(EcomAppApplication.class, args);
    }
    private static void setPropertyIfMissing(String key, String value) {
        if (System.getenv(key) == null && System.getProperty(key) == null && value != null) {
            System.setProperty(key, value);
        }
    }

}
