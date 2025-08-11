package com.example.courses.service;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.user.CreateUserRequest;
import com.example.courses.model.dto.request.user.UpdateUserInfoRequest;
import com.example.courses.model.dto.request.auth.ChangePasswordRequest;
import com.example.courses.model.dto.response.user.UserDetailResponse;
import com.example.courses.model.dto.response.user.UserListResponse;
import com.example.courses.model.entity.Role;

/**
 * Interface for user management service
 */
public interface IUserService {

    /**
     * Lấy danh sách tất cả người dùng với phân trang và lọc
     * @param page số trang
     * @param size kích thước trang
     * @param sortBy field để sắp xếp
     * @param sortDir hướng sắp xếp (asc/desc)
     * @param role lọc theo vai trò
     * @param isActive lọc theo trạng thái
     * @param keyword từ khóa tìm kiếm
     * @return UserListResponse danh sách user
     */
    UserListResponse getAllUsers(int page, int size, String sortBy, String sortDir,
                                 Role role, Boolean isActive, String keyword);

    /**
     * Lấy thông tin chi tiết một người dùng
     * @param userId ID của user
     * @return UserDetailResponse thông tin chi tiết user
     * @throws NotFoundException nếu không tìm thấy user
     */
    UserDetailResponse getUserById(Integer userId) throws NotFoundException;

    /**
     * Tạo tài khoản người dùng mới
     * @param request thông tin tạo user
     * @return UserDetailResponse user được tạo
     * @throws BadRequestException nếu dữ liệu không hợp lệ
     */
    UserDetailResponse createUser(CreateUserRequest request) throws BadRequestException;

    /**
     * Cập nhật thông tin cá nhân của người dùng
     * @param userId ID của user
     * @param request thông tin cập nhật
     * @param currentUsername username của người thực hiện request
     * @return UserDetailResponse user sau khi update
     * @throws NotFoundException nếu không tìm thấy user
     * @throws BadRequestException nếu không có quyền hoặc dữ liệu không hợp lệ
     */
    UserDetailResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request, String currentUsername)
            throws NotFoundException, BadRequestException;

    /**
     * Đổi mật khẩu của người dùng
     * @param userId ID của user
     * @param request thông tin đổi mật khẩu
     * @param currentUsername username của người thực hiện request
     * @throws NotFoundException nếu không tìm thấy user
     * @throws BadRequestException nếu không có quyền hoặc dữ liệu không hợp lệ
     */
    void changePassword(Integer userId, ChangePasswordRequest request, String currentUsername)
            throws NotFoundException, BadRequestException;

    /**
     * Cập nhật vai trò của người dùng
     * @param userId ID của user
     * @param newRole vai trò mới
     * @return UserDetailResponse user sau khi update
     * @throws NotFoundException nếu không tìm thấy user
     */
    UserDetailResponse updateUserRole(Integer userId, Role newRole) throws NotFoundException;

    /**
     * Cập nhật trạng thái của người dùng
     * @param userId ID của user
     * @param isActive trạng thái mới
     * @return UserDetailResponse user sau khi update
     * @throws NotFoundException nếu không tìm thấy user
     */
    UserDetailResponse updateUserStatus(Integer userId, Boolean isActive) throws NotFoundException;

    /**
     * Xóa người dùng khỏi hệ thống
     * @param userId ID của user
     * @throws NotFoundException nếu không tìm thấy user
     * @throws BadRequestException nếu không thể xóa do ràng buộc
     */
    void deleteUser(Integer userId) throws NotFoundException, BadRequestException;
}