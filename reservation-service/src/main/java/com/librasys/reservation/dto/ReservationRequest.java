package com.librasys.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public class ReservationRequest {

    @NotBlank(message = "memberId is required")
    private String memberId;

    @NotBlank(message = "bookId is required")
    private String bookId;

    public ReservationRequest() {}

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
}
