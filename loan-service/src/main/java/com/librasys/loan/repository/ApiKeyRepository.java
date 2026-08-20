package com.librasys.loan.repository;

import com.librasys.loan.model.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyAndStatus(String key, String status);
    boolean existsByKey(String key);
}
