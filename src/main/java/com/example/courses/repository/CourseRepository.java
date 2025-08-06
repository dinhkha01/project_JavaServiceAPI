package com.example.courses.repository;

import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer>, JpaSpecificationExecutor<Course> {

    // Tìm khóa học theo trạng thái
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    // Tìm khóa học theo giảng viên
    List<Course> findByTeacherId(Integer teacherId);

    // Tìm khóa học theo giảng viên và trạng thái
    List<Course> findByTeacherIdAndStatus(Integer teacherId, CourseStatus status);

    // Tìm khóa học với thông tin giảng viên
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher WHERE c.courseId = :courseId")
    Optional<Course> findByIdWithTeacher(@Param("courseId") Integer courseId);

    // Tìm khóa học với đầy đủ thông tin (lessons, reviews, enrollments)
    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.teacher " +
            "LEFT JOIN FETCH c.lessons l " +
            "LEFT JOIN FETCH c.reviews r " +
            "LEFT JOIN FETCH c.enrollments e " +
            "WHERE c.courseId = :courseId")
    Optional<Course> findByIdWithFullDetails(@Param("courseId") Integer courseId);

    // Đếm số khóa học theo trạng thái
    long countByStatus(CourseStatus status);

    // Tìm khóa học theo từ khóa trong title hoặc description
    @Query("SELECT c FROM Course c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}