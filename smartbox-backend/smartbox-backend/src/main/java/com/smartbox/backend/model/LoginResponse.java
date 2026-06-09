package com.smartbox.backend.model;

public class LoginResponse {

    private boolean success;

    private String userId;

    private String username;
    private String role;
    private String phone;

    public LoginResponse(
            boolean success,
            String userId,
            String username,
            String role,
            String phone
    ) {
        this.success = success;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.phone = phone;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }
}