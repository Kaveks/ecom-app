package org.example.ecomapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

        SpringApplication.run(EcomAppApplication.class, args);
    }

}
