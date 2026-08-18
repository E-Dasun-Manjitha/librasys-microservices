package com.librasys.auth.repository;

import com.librasys.auth.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
