package com.example.courses.repository;

import com.example.courses.model.dto.response.StudentProgressResponse;
import com.example.courses.model.dto.response.TeacherCoursesOverviewResponse;
import com.example.courses.model.dto.response.TopCourseResponse;
import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    /**
     * Tìm kiếm và lọc khóa học với tất cả các điều kiện
     * Sử dụng điều kiện động - nếu tham số null thì bỏ qua điều kiện đó
     */
    @Query("SELECT c FROM Course c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:teacherId IS NULL OR c.teacherId = :teacherId) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> findCoursesWithFilters(@Param("status") CourseStatus status,
                                        @Param("teacherId") Integer teacherId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    /**
     * Lấy danh sách các khóa học phổ biến nhất theo số lượt đăng ký
     */
    @Query("SELECT new com.example.courses.model.dto.response.TopCourseResponse(" +
            "c.courseId, c.title, c.description, c.teacherId, t.fullName, c.price, " +
            "COUNT(DISTINCT e), " +
            "COALESCE(AVG(r.rating), 0.0), " +
            "COUNT(DISTINCT r), " +
            "COUNT(DISTINCT l), " +
            "SUM(CASE WHEN l.isPublished = true THEN 1 ELSE 0 END), " +
            "COALESCE(SUM(CASE WHEN e.enrollmentId IS NOT NULL THEN c.price ELSE 0 END), 0), " +
            "CAST(c.status AS string), c.createdAt, c.updatedAt) " +
            "FROM Course c " +
            "LEFT JOIN c.teacher t " +
            "LEFT JOIN c.enrollments e " +
            "LEFT JOIN c.reviews r " +
            "LEFT JOIN c.lessons l " +
            "WHERE c.status = 'PUBLISHED' " +
            "GROUP BY c.courseId, c.title, c.description, c.teacherId, t.fullName, c.price, c.status, c.createdAt, c.updatedAt " +
            "ORDER BY COUNT(DISTINCT e) DESC")
    Page<TopCourseResponse> findTopCoursesByEnrollments(Pageable pageable);

    /**
     * Thống kê tiến độ học của một sinh viên cụ thể
     */
    @Query("SELECT new com.example.courses.model.dto.response.StudentProgressResponse(" +
            "u.userId, u.fullName, u.email, " +
            "CAST(COALESCE(COUNT(DISTINCT e), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN e.completionDate IS NOT NULL THEN 1 ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN e.completionDate IS NULL AND e.enrollmentDate IS NOT NULL THEN 1 ELSE 0 END), 0) AS int), " +
            "COALESCE(AVG(CAST(e.progressPercentage AS double)), 0.0), " +
            "null, " + // courseProgress will be set separately
            "MAX(lp.lastAccessedAt)) " +
            "FROM User u " +
            "LEFT JOIN u.enrollments e " +
            "LEFT JOIN e.lessonProgresses lp " +
            "WHERE u.userId = :studentId " +
            "GROUP BY u.userId, u.fullName, u.email")
    StudentProgressResponse getStudentProgressReport(@Param("studentId") Integer studentId);

    /**
     * Thống kê tổng quan về các khóa học của một giảng viên
     */
    @Query("SELECT new com.example.courses.model.dto.response.TeacherCoursesOverviewResponse(" +
            "u.userId, u.fullName, u.email, " +
            "CAST(COALESCE(COUNT(DISTINCT c), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN c.status = 'PUBLISHED' THEN 1 ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN c.status = 'DRAFT' THEN 1 ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(SUM(CASE WHEN c.status = 'ARCHIVED' THEN 1 ELSE 0 END), 0) AS int), " +
            "CAST(COALESCE(COUNT(DISTINCT e), 0) AS int), " +
            "COALESCE(AVG(CAST(r.rating AS double)), 0.0), " +
            "COALESCE(SUM(c.price * SIZE(c.enrollments)), 0), " +
            "null, null, u.createdAt) " + // courses and enrollmentsByMonth will be null
            "FROM User u " +
            "LEFT JOIN u.coursesAsTeacher c " +
            "LEFT JOIN c.enrollments e " +
            "LEFT JOIN c.reviews r " +
            "WHERE u.userId = :teacherId " +
            "GROUP BY u.userId, u.fullName, u.email, u.createdAt")
    TeacherCoursesOverviewResponse getTeacherCoursesOverview(@Param("teacherId") Integer teacherId);
}