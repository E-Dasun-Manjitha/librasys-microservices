package com.librasys.loan.controller;

import com.librasys.loan.client.BookServiceClient;
import com.librasys.loan.client.NotificationServiceClient;
import com.librasys.loan.dto.BookDto;
import com.librasys.loan.dto.LoanRequest;
import com.librasys.loan.model.Loan;
import com.librasys.loan.repository.LoanRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanRepository loanRepository;
    private final BookServiceClient bookServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public LoanController(LoanRepository loanRepository,
                          BookServiceClient bookServiceClient,
                          NotificationServiceClient notificationServiceClient) {
        this.loanRepository = loanRepository;
        this.bookServiceClient = bookServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    // POST /api/loans - borrow a book
    @PostMapping
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody LoanRequest request) {
        // Check book exists and has copies available
        BookDto book;
        try {
            book = bookServiceClient.getBook(request.getBookId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Book not found with id: " + request.getBookId());
        }

        if (book.getCopiesAvailable() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No copies available for book: " + book.getTitle());
        }

        // Decrement copies in book-service
        bookServiceClient.decrementCopies(request.getBookId());

        // Create the loan record
        Loan loan = new Loan(request.getMemberId(), request.getBookId());
        Loan saved = loanRepository.save(loan);

        // Automatically trigger notification for borrowing book
        String message = String.format(
                "You have successfully borrowed '%s' by %s. Due date is %s.",
                book.getTitle(), book.getAuthor(), saved.getDueDate());
        notificationServiceClient.sendEmailNotification(request.getMemberId(), message);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/loans/{id}/return - return a book
    @PutMapping("/{id}/return")
    public Loan returnLoan(@PathVariable String id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Loan not found with id: " + id));

        if ("RETURNED".equals(loan.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This loan has already been returned");
        }

        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus("RETURNED");

        // Increment copies in book-service
        bookServiceClient.incrementCopies(loan.getBookId());
        Loan saved = loanRepository.save(loan);

        // Fetch book title if possible and trigger notification for return
        String bookTitle = "Book #" + loan.getBookId();
        try {
            BookDto book = bookServiceClient.getBook(loan.getBookId());
            if (book != null) {
                bookTitle = "'" + book.getTitle() + "'";
            }
        } catch (Exception ignored) {
        }

        String message = String.format(
                "You have successfully returned %s on %s. Thank you!",
                bookTitle, saved.getReturnDate());
        notificationServiceClient.sendEmailNotification(loan.getMemberId(), message);

        return saved;
    }

    // GET /api/loans/member/{memberId} - list loans for a member
    @GetMapping("/member/{memberId}")
    public List<Loan> getLoansByMember(@PathVariable String memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    // GET /api/loans/overdue - list overdue loans
    @GetMapping("/overdue")
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDateTime.now());
    }
}
