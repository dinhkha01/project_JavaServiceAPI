package com.example.courses.service.impl;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.CreateUserRequest;
import com.example.courses.model.dto.request.UpdateUserInfoRequest;
import com.example.courses.model.dto.request.ChangePasswordRequest;
import com.example.courses.model.dto.response.*;
import com.example.courses.model.entity.Role;
import com.example.courses.model.entity.User;
import com.example.courses.repository.UserRepository;
import com.example.courses.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserListResponse getAllUsers(int page, int size, String sortBy, String sortDir,
                                        Role role, Boolean isActive, String keyword) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = getUsersWithFilters(keyword, role, isActive, pageable);

        List<UserSummaryResponse> users = userPage.getContent().stream()
                .map(this::convertToUserSummaryResponse)
                .collect(Collectors.toList());

        return UserListResponse.builder()
                .users(users)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public UserDetailResponse getUserById(Integer userId) throws NotFoundException {
        User user = findUserByIdOrThrow(userId);
        return convertToUserDetailResponse(user);
    }

    @Override
    public UserDetailResponse createUser(CreateUserRequest request) throws BadRequestException {
        validateUniqueConstraints(request);

        User user = buildUserFromRequest(request);
        User savedUser = userRepository.save(user);

        log.info("Created new user: {} with role: {}", savedUser.getUsername(), savedUser.getRole());
        return convertToUserDetailResponse(savedUser);
    }

    @Override
    public UserDetailResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request, String currentUsername)
            throws NotFoundException, BadRequestException {

        User user = findUserByIdOrThrow(userId);
        User currentUser = getCurrentUser(currentUsername);

        // Kiểm tra quyền: chỉ admin hoặc chính người dùng đó mới có thể cập nhật
        validateOwnershipOrAdmin(user, currentUser);

        // Kiểm tra email có bị trùng không (trừ email hiện tại)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "Email đã được sử dụng");
            throw new RuntimeException("Email đã tồn tại");
        }

        // Cập nhật thông tin
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);

        log.info("Updated user info for: {} by: {}", user.getUsername(), currentUsername);
        return convertToUserDetailResponse(updatedUser);
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request, String currentUsername)
            throws NotFoundException, BadRequestException {

        User user = findUserByIdOrThrow(userId);
        User currentUser = getCurrentUser(currentUsername);

        // Kiểm tra quyền: chỉ admin hoặc chính người dùng đó mới có thể đổi mật khẩu
        validateOwnershipOrAdmin(user, currentUser);

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu không khớp");
        }

        // Nếu không phải admin, phải kiểm tra mật khẩu hiện tại
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            // Kiểm tra mật khẩu hiện tại có được cung cấp không
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("currentPassword", "Mật khẩu hiện tại không được để trống");
                throw new RuntimeException("Mật khẩu hiện tại không được để trống");
            }

            // Kiểm tra mật khẩu hiện tại có đúng không
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                Map<String, String> errors = new HashMap<>();
                errors.put("currentPassword", "Mật khẩu hiện tại không chính xác");
                throw new RuntimeException("Mật khẩu hiện tại không chính xác");
            }
        }

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Changed password for user: {} by: {}", user.getUsername(), currentUsername);
    }

    @Override
    public UserDetailResponse updateUserRole(Integer userId, Role newRole) throws NotFoundException {
        User user = findUserByIdOrThrow(userId);
        Role oldRole = user.getRole();

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        log.info("Updated user {} role from {} to {}", user.getUsername(), oldRole, newRole);
        return convertToUserDetailResponse(updatedUser);
    }

    @Override
    public UserDetailResponse updateUserStatus(Integer userId, Boolean isActive) throws NotFoundException {
        User user = findUserByIdOrThrow(userId);
        Boolean oldStatus = user.getIsActive();

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);

        log.info("Updated user {} status from {} to {}", user.getUsername(), oldStatus, isActive);
        return convertToUserDetailResponse(updatedUser);
    }

    @Override
    public void deleteUser(Integer userId) throws NotFoundException, BadRequestException {
        User user = findUserByIdOrThrow(userId);
        validateUserDeletionConstraints(user);

        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    /**
     * Get current user from username
     */
    private User getCurrentUser(String username) throws NotFoundException, BadRequestException {
        if (username == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng hiện tại"));
    }

    /**
     * Validate that current user is owner or admin
     */
    private void validateOwnershipOrAdmin(User targetUser, User currentUser)  {
        boolean isOwner = Objects.equals(targetUser.getUserId(), currentUser.getUserId());
        boolean isAdmin = currentUser.getRole().equals(Role.ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này");
        }
    }

    /**
     * Get users with applied filters
     */
    private Page<User> getUsersWithFilters(String keyword, Role role, Boolean isActive, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (role != null) {
                return userRepository.searchByKeywordAndRole(keyword.trim(), role, pageable);
            } else {
                return userRepository.searchByKeyword(keyword.trim(), pageable);
            }
        } else if (role != null && isActive != null) {
            return userRepository.findByRoleAndIsActive(role, isActive, pageable);
        } else if (role != null) {
            return userRepository.findByRole(role, pageable);
        } else if (isActive != null) {
            return userRepository.findByIsActive(isActive, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * Find user by ID or throw NotFoundException
     */
    private User findUserByIdOrThrow(Integer userId) throws NotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }

    /**
     * Validate unique constraints for user creation
     */
    private void validateUniqueConstraints(CreateUserRequest request) throws BadRequestException {
        Map<String, String> errors = new HashMap<>();

        if (userRepository.existsByUsername(request.getUsername())) {
            errors.put("username", "Username đã tồn tại");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            errors.put("email", "Email đã được sử dụng");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException("Dữ liệu không hợp lệ", errors);
        }
    }

    /**
     * Build User entity from CreateUserRequest
     */
    private User buildUserFromRequest(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return user;
    }

    /**
     * Validate user deletion constraints
     */
    private void validateUserDeletionConstraints(User user) throws BadRequestException {
        if (user.getCoursesAsTeacher() != null && !user.getCoursesAsTeacher().isEmpty()) {
            throw new RuntimeException("Không thể xóa người dùng vì đang có khóa học được tạo bởi người này");
        }

        if (user.getEnrollments() != null && !user.getEnrollments().isEmpty()) {
            throw new RuntimeException("Không thể xóa người dùng vì đang có đăng ký khóa học");
        }
    }

    /**
     * Convert User entity to UserSummaryResponse DTO
     */
    private UserSummaryResponse convertToUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
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

    /**
     * Convert User entity to UserDetailResponse DTO
     */
    private UserDetailResponse convertToUserDetailResponse(User user) {
        return UserDetailResponse.builder()
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