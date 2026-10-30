package com.smartcampus.dto;

public class JwtResponseDto {
    public String accessToken;
    public String tokenType = "Bearer";

    public JwtResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
