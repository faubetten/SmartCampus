package com.smartcampus.dto;

public class ForgotPasswordResponseDto {
    public String message;
    public String debugOtp;

    public ForgotPasswordResponseDto(String message, String debugOtp) {
        this.message = message;
        this.debugOtp = debugOtp;
    }
}
