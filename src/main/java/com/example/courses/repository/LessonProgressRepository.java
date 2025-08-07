package com.example.courses.repository;

import com.example.courses.model.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {

    /**
     * Tìm lesson progress theo enrollment và lesson
     */
    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Integer enrollmentId, Integer lessonId);

    /**
     * Lấy tất cả lesson progress của một enrollment
     */
    List<LessonProgress> findByEnrollmentIdOrderByLessonOrderIndex(Integer enrollmentId);

    /**
     * Kiểm tra xem lesson đã được hoàn thành chưa
     */
    boolean existsByEnrollmentIdAndLessonIdAndIsCompletedTrue(Integer enrollmentId, Integer lessonId);

    /**
     * Đếm số lesson đã hoàn thành trong enrollment
     */
    long countByEnrollmentIdAndIsCompletedTrue(Integer enrollmentId);

    /**
     * Đếm tổng số lesson trong enrollment
     */
    long countByEnrollmentId(Integer enrollmentId);

    /**
     * Cập nhật trạng thái hoàn thành của lesson
     */
    @Modifying
    @Query("UPDATE LessonProgress lp SET " +
            "lp.isCompleted = :isCompleted, " +
            "lp.completedAt = :completedAt, " +
            "lp.lastAccessedAt = CURRENT_TIMESTAMP " +
            "WHERE lp.enrollmentId = :enrollmentId AND lp.lessonId = :lessonId")
    int updateCompletionStatus(@Param("enrollmentId") Integer enrollmentId,
                               @Param("lessonId") Integer lessonId,
                               @Param("isCompleted") Boolean isCompleted,
                               @Param("completedAt") LocalDateTime completedAt);

    /**
     * Lấy lesson progress với kiểm tra quyền của sinh viên
     */
    @Query("SELECT lp FROM LessonProgress lp " +
            "JOIN lp.enrollment e " +
            "WHERE lp.enrollmentId = :enrollmentId " +
            "AND lp.lessonId = :lessonId " +
            "AND e.student.userId = :studentId")
    Optional<LessonProgress> findByEnrollmentIdAndLessonIdAndStudentId(
            @Param("enrollmentId") Integer enrollmentId,
            @Param("lessonId") Integer lessonId,
            @Param("studentId") Integer studentId);

    /**
     * Tạo lesson progress cho tất cả lessons của course khi student enroll
     */
    @Query(value = "INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed_at) " +
            "SELECT :enrollmentId, l.lesson_id, false, CURRENT_TIMESTAMP " +
            "FROM lesson l " +
            "WHERE l.course_id = :courseId AND l.is_published = true " +
            "ORDER BY l.order_index", nativeQuery = true)
    @Modifying
    void createProgressForAllLessons(@Param("enrollmentId") Integer enrollmentId,
                                     @Param("courseId") Integer courseId);
}