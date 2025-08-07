package com.example.courses.repository;

import com.example.courses.model.entity.Enrollment;
import com.example.courses.model.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    /**
     * Tìm tất cả enrollments của một sinh viên
     */
    @Query("SELECT e FROM Enrollment e " +
            "LEFT JOIN FETCH e.course c " +
            "LEFT JOIN FETCH c.teacher " +
            "WHERE e.student.userId = :studentId")
    Page<Enrollment> findByStudentId(@Param("studentId") Integer studentId, Pageable pageable);

    /**
     * Kiểm tra xem sinh viên đã đăng ký khóa học chưa
     */
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Integer courseId);

    /**
     * Lấy enrollment với kiểm tra quyền của sinh viên
     */
    @Query("SELECT e FROM Enrollment e " +
            "LEFT JOIN FETCH e.course c " +
            "LEFT JOIN FETCH c.teacher " +
            "LEFT JOIN FETCH e.lessonProgresses lp " +
            "LEFT JOIN FETCH lp.lesson " +
            "WHERE e.enrollmentId = :enrollmentId AND e.student.userId = :studentId")
    Optional<Enrollment> findByIdAndStudentId(@Param("enrollmentId") Integer enrollmentId,
                                              @Param("studentId") Integer studentId);


}