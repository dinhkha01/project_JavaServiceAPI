package com.example.courses.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCourseResponse {

    private Integer courseId;
    private String title;
    private String description;
    private Integer teacherId;
    private String teacherName;
    private BigDecimal price;
    private Integer totalEnrollments;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalLessons;
    private Integer publishedLessons;
    private BigDecimal revenue;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructor để sử dụng trong JPQL query
    public TopCourseResponse(Integer courseId, String title, String description,
                             Integer teacherId, String teacherName, BigDecimal price,
                             Long totalEnrollments, Double averageRating,
                             Long totalReviews, Long totalLessons,
                             Long publishedLessons, BigDecimal revenue,
                             String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.price = price;
        this.totalEnrollments = totalEnrollments != null ? totalEnrollments.intValue() : 0;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews != null ? totalReviews.intValue() : 0;
        this.totalLessons = totalLessons != null ? totalLessons.intValue() : 0;
        this.publishedLessons = publishedLessons != null ? publishedLessons.intValue() : 0;
        this.revenue = revenue;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}