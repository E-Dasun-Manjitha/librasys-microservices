package com.librasys.notification.config;

import com.librasys.notification.model.ApiKey;
import com.librasys.notification.repository.ApiKeyRepository;
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
                        "notification-service-key-2026",
                        "notification-service",
                        "Direct Access & Gateway Authorization Key for Notification Service (Student 5)",
                        "ACTIVE"
                ));
                System.out.println(">>> Seeded API Key into MongoDB 'api_keys' collection for Notification Service.");
            }
        };
    }
}
