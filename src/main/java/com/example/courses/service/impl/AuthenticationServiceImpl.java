package com.example.courses.service.impl;

import com.example.courses.model.dto.request.FormLogin;
import com.example.courses.model.dto.request.FormRegister;
import com.example.courses.model.dto.request.TokenVerifyRequest;
import com.example.courses.model.dto.response.JwtResponse;
import com.example.courses.model.dto.response.TokenVerifyResponse;
import com.example.courses.model.dto.response.UserProfileResponse;
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

import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final IAccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;

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
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
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