package com.librasys.notification.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {

    @NotBlank(message = "memberId is required")
    private String memberId;

    @NotBlank(message = "message is required")
    private String message;

    public NotificationRequest() {}

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
