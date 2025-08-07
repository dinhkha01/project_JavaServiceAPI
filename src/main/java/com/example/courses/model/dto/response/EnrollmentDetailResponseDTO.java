package com.example.courses.model.dto.response;

import com.example.courses.model.entity.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDetailResponseDTO {
    private Integer enrollmentId;
    private Integer studentId;
    private String studentName;
    private CourseBasicDTO course;
    private EnrollmentStatus status;
    private BigDecimal progressPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime enrollmentDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completionDate;

    private List<LessonProgressDTO> lessonProgresses;
}