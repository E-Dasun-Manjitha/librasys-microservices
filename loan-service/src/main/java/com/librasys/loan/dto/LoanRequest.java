package com.librasys.loan.dto;

import jakarta.validation.constraints.NotBlank;

public class LoanRequest {

    @NotBlank(message = "memberId is required")
    private String memberId;

    @NotBlank(message = "bookId is required")
    private String bookId;

    public LoanRequest() {}

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
}
