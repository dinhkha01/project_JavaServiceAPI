package com.example.courses.service.impl;

import com.example.courses.model.dto.request.auth.FormLogin;
import com.example.courses.model.dto.request.auth.FormRegister;
import com.example.courses.model.dto.request.auth.LogoutRequest;
import com.example.courses.model.dto.request.auth.TokenVerifyRequest;
import com.example.courses.model.dto.response.auth.JwtResponse;
import com.example.courses.model.dto.response.auth.LogoutResponse;
import com.example.courses.model.dto.response.auth.TokenVerifyResponse;
import com.example.courses.model.dto.response.user.UserProfileResponse;
import com.example.courses.model.entity.Role;
import com.example.courses.model.entity.User;
import com.example.courses.repository.IAccountRepository;
import com.example.courses.config.security.jwt.JWTProvider;
import com.example.courses.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final IAccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public JwtResponse login(FormLogin request) {
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            );
            Authentication auth = authenticationManager.authenticate(authentication);
            User account = accountRepository.loadUserByUsername(request.getUsername())
                    .orElseThrow(() -> new NoSuchElementException("Người dùng không tồn tại"));

            UserProfileResponse userProfile = buildUserProfileResponse(account);

            return JwtResponse.builder()
                    .accessToken(jwtProvider.generateToken((UserDetails) auth.getPrincipal()))
                    .account(userProfile)
                    .build();
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            throw new RuntimeException("Đăng nhập thất bại: " + "email hoặc mật khẩu không đúng");
        }
    }

    @Override
    public User register(FormRegister request) {
        validateUserRegistration(request);

        User account = new User();
        account.setUsername(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setEmail(request.getEmail());
        account.setFullName(request.getFullName());
        account.setRole(Role.ROLE_STUDENT);
        account.setIsActive(true);

        User savedUser = accountRepository.save(account);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return savedUser;
    }

    @Override
    public TokenVerifyResponse verifyToken(TokenVerifyRequest request) {
        try {
            // Kiểm tra token có trong blacklist không
            if (tokenBlacklistService.isTokenBlacklisted(request.getToken())) {
                return TokenVerifyResponse.builder()
                        .valid(false)
                        .message("Token đã bị vô hiệu hóa")
                        .build();
            }

            boolean isValid = jwtProvider.validateToken(request.getToken());
            if (isValid) {
                String username = jwtProvider.getUserNameFromToken(request.getToken());
                User user = accountRepository.loadUserByUsername(username)
                        .orElseThrow(() -> new NoSuchElementException("Người dùng không tồn tại"));

                UserProfileResponse currentUser = buildUserProfileResponse(user);

                return TokenVerifyResponse.builder()
                        .valid(true)
                        .user(currentUser)
                        .message("Token hợp lệ")
                        .build();
            } else {
                return TokenVerifyResponse.builder()
                        .valid(false)
                        .message("Token không hợp lệ hoặc đã hết hạn")
                        .build();
            }
        } catch (Exception e) {
            log.error("Token verification failed", e);
            return TokenVerifyResponse.builder()
                    .valid(false)
                    .message("Token không hợp lệ: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = accountRepository.loadUserByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Người dùng không tồn tại"));

        return buildUserProfileResponse(user);
    }

    @Override
    public LogoutResponse logout(LogoutRequest request, String username) {
        try {
            // Validate token thuộc về user hiện tại
            if (request.getToken() != null && !request.getToken().isEmpty()) {
                String tokenUsername = jwtProvider.getUserNameFromToken(request.getToken());
                if (!username.equals(tokenUsername)) {
                    throw new RuntimeException("Token không thuộc về user hiện tại");
                }
            }
            boolean loggedOutFromAllDevices = false;

            // Blacklist token hiện tại
            if (request.getToken() != null) {
                tokenBlacklistService.blacklistToken(request.getToken());
            }

            log.info("User {} logged out successfully", username);

            return LogoutResponse.builder()
                    .username(username)
                    .logoutTime(LocalDateTime.now())
                    .message("Đăng xuất thành công")
                    .loggedOutFromAllDevices(loggedOutFromAllDevices)
                    .deviceId(request.getDeviceId())
                    .build();

        } catch (Exception e) {
            log.error("Logout failed for user: {}", username, e);
            throw new RuntimeException("Đăng xuất thất bại: " + e.getMessage());
        }
    }

    @Override
    public LogoutResponse logoutFromAllDevices(String username) {
        try {
            // Trong thực tế, cần implement logic để:
            // 1. Tìm tất cả token active của user
            // 2. Blacklist tất cả token đó
            // 3. Hoặc thay đổi user's secret key để invalidate tất cả token

            log.info("User {} logged out from all devices", username);

            return LogoutResponse.builder()
                    .username(username)
                    .logoutTime(LocalDateTime.now())
                    .message("Đăng xuất khỏi tất cả thiết bị thành công")
                    .loggedOutFromAllDevices(true)
                    .build();

        } catch (Exception e) {
            log.error("Logout from all devices failed for user: {}", username, e);
            throw new RuntimeException("Đăng xuất khỏi tất cả thiết bị thất bại: " + e.getMessage());
        }
    }

    /**
     * Validate user registration data
     */
    private void validateUserRegistration(FormRegister request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại trong hệ thống");
        }
    }

    /**
     * Build UserProfileResponse from User entity
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}