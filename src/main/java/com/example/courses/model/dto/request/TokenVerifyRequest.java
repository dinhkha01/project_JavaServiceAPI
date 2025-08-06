package com.example.courses.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenVerifyRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;
}