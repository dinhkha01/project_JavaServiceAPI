package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCoursesOverviewResponse {

    private Integer teacherId;
    private String teacherName;
    private String teacherEmail;
    private Integer totalCourses;
    private Integer publishedCourses;
    private Integer draftCourses;
    private Integer archivedCourses;
    private Integer totalEnrollments;
    private Double averageRating;
    private BigDecimal totalRevenue;
    private List<TeacherCourseDetail> courses;
    private Map<String, Integer> enrollmentsByMonth;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherCourseDetail {
        private Integer courseId;
        private String title;
        private String status;
        private BigDecimal price;
        private Integer totalLessons;
        private Integer publishedLessons;
        private Integer enrollments;
        private Double averageRating;
        private Integer totalReviews;
        private BigDecimal revenue;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}