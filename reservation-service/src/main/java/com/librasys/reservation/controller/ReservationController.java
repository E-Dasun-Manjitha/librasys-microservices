package com.librasys.reservation.controller;

import com.librasys.reservation.client.BookServiceClient;
import com.librasys.reservation.client.NotificationServiceClient;
import com.librasys.reservation.dto.BookDto;
import com.librasys.reservation.dto.ReservationRequest;
import com.librasys.reservation.model.Reservation;
import com.librasys.reservation.repository.ReservationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final BookServiceClient bookServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public ReservationController(ReservationRepository reservationRepository,
                                  BookServiceClient bookServiceClient,
                                  NotificationServiceClient notificationServiceClient) {
        this.reservationRepository = reservationRepository;
        this.bookServiceClient = bookServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    // POST /api/reservations - create a reservation
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        // Verify the book exists
        BookDto book;
        try {
            book = bookServiceClient.getBook(request.getBookId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Book not found with id: " + request.getBookId());
        }

        Reservation reservation = new Reservation(request.getMemberId(), request.getBookId());
        Reservation saved = reservationRepository.save(reservation);

        // Automatically trigger notification when book reservation is created
        String message = String.format(
                "You have placed a reservation for '%s' by %s. We will notify you when a copy becomes available.",
                book.getTitle(), book.getAuthor());
        try {
            notificationServiceClient.sendEmailNotification(request.getMemberId(), message);
        } catch (Exception ignored) {
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // DELETE /api/reservations/{id} - cancel a reservation
    @DeleteMapping("/{id}")
    public Reservation cancelReservation(@PathVariable String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservation not found with id: " + id));

        reservation.setStatus("CANCELLED");
        Reservation saved = reservationRepository.save(reservation);

        // Optional: notify cancellation
        try {
            String message = "Your reservation #" + id + " has been cancelled.";
            notificationServiceClient.sendEmailNotification(reservation.getMemberId(), message);
        } catch (Exception ignored) {
        }

        return saved;
    }

    // GET /api/reservations/member/{memberId} - list reservations for a member
    @GetMapping("/member/{memberId}")
    public List<Reservation> getReservationsByMember(@PathVariable String memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    // POST /api/reservations/{id}/notify - check availability and notify member
    @PostMapping("/{id}/notify")
    public Reservation notifyReservation(@PathVariable String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reservation not found with id: " + id));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Reservation is not in PENDING status");
        }

        // Check if book now has copies available
        BookDto book;
        try {
            book = bookServiceClient.getBook(reservation.getBookId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Book not found with id: " + reservation.getBookId());
        }

        if (book.getCopiesAvailable() > 0) {
            // Send notification via notification-service
            String message = String.format(
                    "Good news! The book '%s' by %s is now available. " +
                    "Please visit the library to borrow your copy.",
                    book.getTitle(), book.getAuthor());

            try {
                notificationServiceClient.sendEmailNotification(
                        reservation.getMemberId(), message);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }

            reservation.setStatus("FULFILLED");
            return reservationRepository.save(reservation);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Book still has no copies available");
        }
    }
}
