package com.example.courses.repository;

import com.example.courses.exception.BadRequestException;
import com.example.courses.model.entity.Role;
import com.example.courses.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Kiểm tra username đã tồn tại
     */
    boolean existsByUsername(String username);


    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Lọc user theo role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Lọc user theo trạng thái active
     */
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Lọc user theo role và trạng thái active
     */
    Page<User> findByRoleAndIsActive(Role role, Boolean isActive, Pageable pageable);

    /**
     * Tìm kiếm user theo tên hoặc email (không phân biệt hoa thường)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm kiếm và lọc theo role
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.role = :role AND (" +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByKeywordAndRole(@Param("keyword") String keyword,
                                      @Param("role") Role role,
                                      Pageable pageable);

    Optional<User> findByUsername(String username);
}