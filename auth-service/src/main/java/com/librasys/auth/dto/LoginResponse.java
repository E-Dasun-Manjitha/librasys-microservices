package com.librasys.auth.dto;

public class LoginResponse {

    private String token;
    private String memberId;
    private String email;
    private String name;

    public LoginResponse() {}

    public LoginResponse(String token, String memberId, String email, String name) {
        this.token = token;
        this.memberId = memberId;
        this.email = email;
        this.name = name;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
