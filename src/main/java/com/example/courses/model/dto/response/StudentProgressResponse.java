package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {

    private Integer studentId;
    private String studentName;
    private String studentEmail;
    private Integer totalCoursesEnrolled;
    private Integer completedCourses;
    private Integer inProgressCourses;
    private Double overallProgressPercentage;
    private List<CourseProgressDetail> courseProgress;
    private LocalDateTime lastActivityAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseProgressDetail {
        private Integer courseId;
        private String courseTitle;
        private String teacherName;
        private Integer totalLessons;
        private Integer completedLessons;
        private Double progressPercentage;
        private String enrollmentStatus;
        private LocalDateTime enrolledAt;
        private LocalDateTime lastAccessedAt;
    }
}