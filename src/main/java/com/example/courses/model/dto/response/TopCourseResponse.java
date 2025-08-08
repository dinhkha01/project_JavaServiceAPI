package com.example.courses.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCourseResponse {

    private Integer courseId;
    private String title;
    private String description;
    private Integer teacherId;
    private String teacherName;
    private BigDecimal price;
    private Long totalEnrollments;
    private Double averageRating;
    private Long totalReviews;
    private Long totalLessons;
    private Long publishedLessons;
    private BigDecimal revenue;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;


}