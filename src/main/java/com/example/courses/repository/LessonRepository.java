package com.example.courses.repository;

import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    // Tìm bài học theo khóa học, sắp xếp theo thứ tự
    List<Lesson> findByCourseOrderByOrderIndexAsc(Course course);

    // Tìm bài học đã xuất bản theo khóa học
    List<Lesson> findByCourseAndIsPublishedTrueOrderByOrderIndexAsc(Course course);

    // Đếm số bài học trong khóa học
    long countByCourse(Course course);

    // Đếm số bài học đã xuất bản trong khóa học
    long countByCourseAndIsPublishedTrue(Course course);

    // Tìm bài học theo khóa học và trạng thái xuất bản
    List<Lesson> findByCourseAndIsPublished(Course course, Boolean isPublished);

    // Kiểm tra orderIndex đã tồn tại trong khóa học chưa
    boolean existsByCourseAndOrderIndex(Course course, Integer orderIndex);

    // Tìm bài học có orderIndex lớn nhất trong khóa học
    @Query("SELECT l FROM Lesson l WHERE l.course = :course ORDER BY l.orderIndex DESC")
    List<Lesson> findTopByCourseOrderByOrderIndexDesc(@Param("course") Course course);
    // Tìm tất cả bài học của một khóa học, sắp xếp theo thứ tự
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Integer courseId);

    // Tìm bài học đã xuất bản của một khóa học
    List<Lesson> findByCourseIdAndIsPublishedTrueOrderByOrderIndexAsc(Integer courseId);

    // Kiểm tra trùng orderIndex trong một khóa học
    boolean existsByCourseIdAndOrderIndex(Integer courseId, Integer orderIndex);

    // Kiểm tra trùng orderIndex khi cập nhật (loại trừ lesson hiện tại)
    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.courseId = :courseId AND l.orderIndex = :orderIndex AND l.lessonId != :lessonId")
    boolean existsByCourseIdAndOrderIndexAndLessonIdNot(@Param("courseId") Integer courseId,
                                                        @Param("orderIndex") Integer orderIndex,
                                                        @Param("lessonId") Integer lessonId);

    // Đếm tổng số bài học của một khóa học
    long countByCourseId(Integer courseId);

    // Đếm số bài học đã xuất bản của một khóa học
    long countByCourseIdAndIsPublishedTrue(Integer courseId);

    // Tìm lesson với thông tin course
    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.lessonId = :lessonId")
    Optional<Lesson> findByIdWithCourse(@Param("lessonId") Integer lessonId);

    // Tìm orderIndex lớn nhất trong một khóa học (để thêm bài học cuối)
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.courseId = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Integer courseId);

    // Xóa tất cả bài học của một khóa học
    void deleteByCourseId(Integer courseId);
}
