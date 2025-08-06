package com.example.courses.service;

import com.example.courses.model.dto.request.FormLogin;
import com.example.courses.model.dto.request.FormRegister;
import com.example.courses.model.dto.request.TokenVerifyRequest;
import com.example.courses.model.dto.response.JwtResponse;
import com.example.courses.model.dto.response.TokenVerifyResponse;
import com.example.courses.model.dto.response.UserProfileResponse;
import com.example.courses.model.entity.User;

/**
 * Interface for authentication service
 */
public interface IAuthenticationService {

    /**
     * Đăng nhập và trả về JWT token
     * @param request thông tin đăng nhập
     * @return JwtResponse chứa token và thông tin user
     */
    JwtResponse login(FormLogin request);

    /**
     * Đăng ký tài khoản mới
     * @param request thông tin đăng ký
     * @return User được tạo
     */
    User register(FormRegister request);

    /**
     * Xác thực token
     * @param request token cần xác thực
     * @return TokenVerifyResponse kết quả xác thực
     */
    TokenVerifyResponse verifyToken(TokenVerifyRequest request);

    /**
     * Lấy thông tin profile của user hiện tại
     * @param username tên đăng nhập
     * @return UserProfileResponse thông tin user
     */
    UserProfileResponse getCurrentUserProfile(String username);
}