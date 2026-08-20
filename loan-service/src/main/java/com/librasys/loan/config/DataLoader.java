package com.librasys.loan.config;

import com.librasys.loan.model.ApiKey;
import com.librasys.loan.repository.ApiKeyRepository;
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
                        "loan-service-key-2026",
                        "loan-service",
                        "Direct Access & Gateway Authorization Key for Loan Service (Student 3)",
                        "ACTIVE"
                ));
                System.out.println(">>> Seeded API Key into MongoDB 'api_keys' collection for Loan Service.");
            }
        };
    }
}
