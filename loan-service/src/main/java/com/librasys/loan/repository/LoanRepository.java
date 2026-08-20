package com.librasys.loan.repository;

import com.librasys.loan.model.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {

    List<Loan> findByMemberId(String memberId);

    @Query("{ 'dueDate': { $lt: ?0 }, 'status': 'ACTIVE' }")
    List<Loan> findOverdueLoans(java.time.LocalDateTime currentDate);
}
