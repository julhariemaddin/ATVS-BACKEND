package com.example.acquitance.dto;

public class AuthResultDto {
    private String cookies;
    private String fullName;

    public AuthResultDto(String cookies, String fullName) {
        this.cookies = cookies;
        this.fullName = fullName;
    }

    public String getCookies() { return cookies; }
    public String getFullName() { return fullName; }
}
