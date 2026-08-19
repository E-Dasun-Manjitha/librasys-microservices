package com.librasys.reservation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reservations")
public class Reservation {

    @Id
    private String id;

    private String memberId;

    private String bookId;

    private LocalDateTime reservationDate;

    private String status; // PENDING, FULFILLED, CANCELLED

    public Reservation() {}

    public Reservation(String memberId, String bookId) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.reservationDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
