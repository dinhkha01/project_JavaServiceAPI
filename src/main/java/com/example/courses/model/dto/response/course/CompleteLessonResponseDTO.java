package com.example.courses.model.dto.response.course;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteLessonResponseDTO {
    private Integer progressId;
    private Integer enrollmentId;
    private Integer lessonId;
    private String lessonTitle;
    private Boolean isCompleted;
    private BigDecimal newProgressPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAccessedAt;
}