package com.example.courses.model.dto.response.auth;

import com.example.courses.model.dto.response.user.UserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyResponse {
    private boolean valid;
    private UserProfileResponse user;
    private String message;
}