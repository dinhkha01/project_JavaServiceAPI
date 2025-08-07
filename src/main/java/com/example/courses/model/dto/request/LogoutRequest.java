package com.example.courses.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequest {

    @NotBlank(message = "Token không được để trống")
    private String token;

    /**
     * Optional: thiết bị hoặc session identifier
     * Có thể dùng để logout khỏi thiết bị cụ thể
     */
    private String deviceId;

    /**
     * Optional: logout khỏi tất cả thiết bị
     * Nếu true, sẽ invalidate tất cả token của user
     */
    private boolean logoutFromAllDevices = false;
}