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
public class JwtResponse {
    @Builder.Default
    private final String type = "Bearer Token";
    private String accessToken;
    private UserProfileResponse account;
}