package com.example.courses.repository;

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
}