package com.example.courses.service;

import com.example.courses.model.dto.request.auth.FormLogin;
import com.example.courses.model.dto.request.auth.FormRegister;
import com.example.courses.model.dto.request.auth.LogoutRequest;
import com.example.courses.model.dto.request.auth.TokenVerifyRequest;
import com.example.courses.model.dto.response.auth.JwtResponse;
import com.example.courses.model.dto.response.auth.LogoutResponse;
import com.example.courses.model.dto.response.auth.TokenVerifyResponse;
import com.example.courses.model.dto.response.user.UserProfileResponse;
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

    /**
     * Đăng xuất và invalidate token
     * @param request thông tin đăng xuất
     * @param username tên user hiện tại
     * @return LogoutResponse kết quả đăng xuất
     */
    LogoutResponse logout(LogoutRequest request, String username);

    /**
     * Đăng xuất khỏi tất cả thiết bị
     * @param username tên user cần logout
     * @return LogoutResponse kết quả đăng xuất
     */
    LogoutResponse logoutFromAllDevices(String username);
}