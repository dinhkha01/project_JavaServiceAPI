package com.example.courses.repository;

import com.example.courses.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Lấy tất cả reviews của một khóa học có phân trang
     */
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.student s " +
            "WHERE r.courseId = :courseId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByCourseIdOrderByCreatedAtDesc(@Param("courseId") Integer courseId, Pageable pageable);

    /**
     * Lấy tất cả reviews của một khóa học không phân trang
     */
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.student s " +
            "WHERE r.courseId = :courseId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByCourseIdOrderByCreatedAtDesc(@Param("courseId") Integer courseId);

    /**
     * Kiểm tra xem student đã review khóa học này chưa
     */
    boolean existsByCourseIdAndStudentId(Integer courseId, Integer studentId);

    /**
     * Lấy review theo ID với thông tin student
     */
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.student s " +
            "WHERE r.reviewId = :reviewId")
    Optional<Review> findByIdWithStudent(@Param("reviewId") Integer reviewId);

    /**
     * Lấy tất cả reviews của một student
     */
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.course c " +
            "LEFT JOIN FETCH c.teacher t " +
            "WHERE r.studentId = :studentId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByStudentIdOrderByCreatedAtDesc(@Param("studentId") Integer studentId, Pageable pageable);

    /**
     * Tính điểm trung bình của một khóa học
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Integer courseId);

    /**
     * Đếm số lượng reviews của một khóa học
     */
    long countByCourseId(Integer courseId);
}