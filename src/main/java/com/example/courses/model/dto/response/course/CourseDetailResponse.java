package com.example.courses.model.dto.response.course;

import com.example.courses.model.dto.response.lesson.LessonResponse;
import com.example.courses.model.dto.response.review.ReviewResponse;
import com.example.courses.model.entity.CourseStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponse {
    private int courseId;
    private String title;
    private String description;
    private int teacherId;
    private String teacherName;
    private String teacherEmail;
    private BigDecimal price;
    private int durationHours;
    private CourseStatus status;
    private int totalLessons;
    private int publishedLessons;
    private Double averageRating;
    private int totalEnrollments;
    private List<LessonResponse> publishedLessonss;
    private List<ReviewResponse> reviews;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}