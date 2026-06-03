package com.smartbox.backend.model;

public class LoginResponse {

    private boolean success;

    private String userId;

    private String username;

    public LoginResponse(
            boolean success,
            String userId,
            String username
    ) {
        this.success = success;
        this.userId = userId;
        this.username = username;
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
}