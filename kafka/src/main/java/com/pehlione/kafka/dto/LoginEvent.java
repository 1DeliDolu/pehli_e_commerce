package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public class LoginEvent {

    private String username;
    private String role;
    private String status;
    private LocalDateTime loginTime;

    public LoginEvent() {
    }

    public LoginEvent(String username, String role, String status, LocalDateTime loginTime) {
        this.username = username;
        this.role = role;
        this.status = status;
        this.loginTime = loginTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}
