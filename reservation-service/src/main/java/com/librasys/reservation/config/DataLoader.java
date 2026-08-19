package com.librasys.reservation.config;

import com.librasys.reservation.model.ApiKey;
import com.librasys.reservation.repository.ApiKeyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initApiKeys(ApiKeyRepository apiKeyRepository) {
        return args -> {
            if (apiKeyRepository.count() == 0) {
                apiKeyRepository.save(new ApiKey(
                        "reservation-service-key-2026",
                        "reservation-service",
                        "Direct Access & Gateway Authorization Key for Reservation Service (Student 4)",
                        "ACTIVE"
                ));
                System.out.println(">>> Seeded API Key into MongoDB 'api_keys' collection for Reservation Service.");
            }
        };
    }
}
