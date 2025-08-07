package com.example.courses.controller;

import com.example.courses.model.dto.request.FormLogin;
import com.example.courses.model.dto.request.FormRegister;
import com.example.courses.model.dto.request.LogoutRequest;
import com.example.courses.model.dto.request.TokenVerifyRequest;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.JwtResponse;
import com.example.courses.model.dto.response.LogoutResponse;
import com.example.courses.model.dto.response.TokenVerifyResponse;
import com.example.courses.model.dto.response.UserProfileResponse;
import com.example.courses.model.entity.User;
import com.example.courses.config.security.principal.UserDetailsCus;
import com.example.courses.service.IAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthenticationService authenticationService;

    /**
     * Đăng nhập và nhận token xác thực (JWT)
     */
    @PostMapping("/login")
    public ResponseEntity<DataResponse<JwtResponse>> login(@Valid @RequestBody FormLogin request) {
        try {
            JwtResponse jwtResponse = authenticationService.login(request);
            return ResponseEntity.ok(DataResponse.success(jwtResponse, "Đăng nhập thành công"));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(DataResponse.error("Đăng nhập thất bại: " + e.getMessage()));
        }
    }

    /**
     * Đăng ký tài khoản mới (mặc định role STUDENT)
     */
    @PostMapping("/register")
    public ResponseEntity<DataResponse<User>> register(@Valid @RequestBody FormRegister request) {
        try {
            User newUser = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DataResponse.success(newUser, "Đăng ký tài khoản thành công"));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Đăng ký thất bại: " + e.getMessage()));
        }
    }

    /**
     * Đăng xuất và invalidate token
     */
    @PostMapping("/logout")
    public ResponseEntity<DataResponse<LogoutResponse>> logout(
            @Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal UserDetailsCus userDetails,
            HttpServletRequest httpRequest) {

        try {
            // Nếu request không có token, lấy từ header
            if (request.getToken() == null || request.getToken().isEmpty()) {
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    request.setToken(authHeader.substring(7));
                }
            }

            LogoutResponse logoutResponse = authenticationService.logout(request, userDetails.getUsername());

            log.info("User {} logged out successfully", userDetails.getUsername());

            return ResponseEntity.ok(DataResponse.success(logoutResponse, "Đăng xuất thành công"));

        } catch (Exception e) {
            log.error("Logout failed for user {}: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Đăng xuất thất bại: " + e.getMessage()));
        }
    }

    /**
     * Đăng xuất khỏi tất cả thiết bị
     */
    @PostMapping("/logout/all")
    public ResponseEntity<DataResponse<LogoutResponse>> logoutFromAllDevices(
            @AuthenticationPrincipal UserDetailsCus userDetails) {

        try {
            LogoutResponse logoutResponse = authenticationService.logoutFromAllDevices(userDetails.getUsername());

            log.info("User {} logged out from all devices", userDetails.getUsername());

            return ResponseEntity.ok(DataResponse.success(logoutResponse, "Đăng xuất khỏi tất cả thiết bị thành công"));

        } catch (Exception e) {
            log.error("Logout from all devices failed for user {}: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Đăng xuất khỏi tất cả thiết bị thất bại: " + e.getMessage()));
        }
    }

    /**
     * Xác thực token người dùng
     */
    @PostMapping("/verify")
    public ResponseEntity<DataResponse<TokenVerifyResponse>> verifyToken(@Valid @RequestBody TokenVerifyRequest request) {
        try {
            TokenVerifyResponse verifyResponse = authenticationService.verifyToken(request);
            if (verifyResponse.isValid()) {
                return ResponseEntity.ok(DataResponse.success(verifyResponse, "Token hợp lệ"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DataResponse.error("Token không hợp lệ"));
            }
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi xác thực token: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin hồ sơ của người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<DataResponse<UserProfileResponse>> getCurrentUserProfile(@AuthenticationPrincipal UserDetailsCus userDetails) {
        try {
            UserProfileResponse profile = authenticationService.getCurrentUserProfile(userDetails.getUsername());
            return ResponseEntity.ok(DataResponse.success(profile, "Lấy thông tin hồ sơ thành công"));
        } catch (Exception e) {
            log.error("Get profile failed for user {}: {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không thể lấy thông tin hồ sơ: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint để kiểm tra authentication
     */
    @GetMapping("/hello")
    public ResponseEntity<DataResponse<String>> hello(@AuthenticationPrincipal UserDetailsCus userDetails) {
        String message = String.format("Hello %s! You are authenticated successfully.", userDetails.getUsername());
        return ResponseEntity.ok(DataResponse.success(message, "Xác thực thành công"));
    }
}