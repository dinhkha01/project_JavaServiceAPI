package com.example.courses.controller;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.CreateUserRequest;
import com.example.courses.model.dto.request.UpdateUserRoleRequest;
import com.example.courses.model.dto.request.UpdateUserStatusRequest;
import com.example.courses.model.dto.request.UpdateUserInfoRequest;
import com.example.courses.model.dto.request.ChangePasswordRequest;
import com.example.courses.model.dto.response.*;
import com.example.courses.model.entity.Role;
import com.example.courses.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    /**
     * GET /api/users - Lấy danh sách tất cả người dùng
     */
    @GetMapping
    public ResponseEntity<DataResponse<UserListResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword) {

        try {
            UserListResponse userList = userService.getAllUsers(
                    page, size, sortBy, sortDir, role, isActive, keyword
            );

            return ResponseEntity.ok(DataResponse.success(
                    userList,
                    "Lấy danh sách người dùng thành công"
            ));

        } catch (Exception e) {
            log.error("Error getting user list: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy danh sách người dùng"));
        }
    }

    /**
     * GET /api/users/{user_id} - Lấy thông tin chi tiết một người dùng
     */
    @GetMapping("/{userId}")
    public ResponseEntity<DataResponse<UserDetailResponse>> getUserById(
            @PathVariable Integer userId) throws NotFoundException {

        UserDetailResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(DataResponse.success(
                user,
                "Lấy thông tin người dùng thành công"
        ));
    }

    /**
     * POST /api/users - Tạo tài khoản người dùng mới
     */
    @PostMapping
    public ResponseEntity<DataResponse<UserDetailResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) throws BadRequestException {

        UserDetailResponse newUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(
                        newUser,
                        "Tạo tài khoản người dùng thành công"
                ));
    }

    /**
     * PUT /api/users/{user_id} - Cập nhật thông tin cá nhân của người dùng
     */
    @PutMapping("/{userId}")
    public ResponseEntity<DataResponse<UserDetailResponse>> updateUserInfo(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserInfoRequest request,
            Authentication authentication) throws NotFoundException, BadRequestException {

        String currentUsername = getCurrentUsername(authentication);
        UserDetailResponse updatedUser = userService.updateUserInfo(userId, request, currentUsername);

        return ResponseEntity.ok(DataResponse.success(
                updatedUser,
                "Cập nhật thông tin người dùng thành công"
        ));
    }

    /**
     * PUT /api/users/{user_id}/password - Đổi mật khẩu của người dùng
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<DataResponse<Void>> changePassword(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) throws NotFoundException, BadRequestException {

        String currentUsername = getCurrentUsername(authentication);
        userService.changePassword(userId, request, currentUsername);

        return ResponseEntity.ok(DataResponse.success(
                null,
                "Đổi mật khẩu thành công"
        ));
    }

    /**
     * PUT /api/users/{user_id}/role - Cập nhật vai trò của người dùng
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<DataResponse<UserDetailResponse>> updateUserRole(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserRoleRequest request) throws NotFoundException {

        UserDetailResponse updatedUser = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(DataResponse.success(
                updatedUser,
                "Cập nhật vai trò người dùng thành công"
        ));
    }

    /**
     * PUT /api/users/{user_id}/status - Cập nhật trạng thái của người dùng
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<DataResponse<UserDetailResponse>> updateUserStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserStatusRequest request) throws NotFoundException {

        UserDetailResponse updatedUser = userService.updateUserStatus(userId, request.getIsActive());
        String message = request.getIsActive() ?
                "Kích hoạt tài khoản thành công" :
                "Vô hiệu hóa tài khoản thành công";

        return ResponseEntity.ok(DataResponse.success(updatedUser, message));
    }

    /**
     * DELETE /api/users/{user_id} - Xóa người dùng khỏi hệ thống
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<DataResponse<Void>> deleteUser(
            @PathVariable Integer userId) throws NotFoundException, BadRequestException {

        userService.deleteUser(userId);
        return ResponseEntity.ok(DataResponse.success(null, "Xóa người dùng thành công"));
    }

    /**
     * Helper method to get current username from authentication
     */
    private String getCurrentUsername(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }
}