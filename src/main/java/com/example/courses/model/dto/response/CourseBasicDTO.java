package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseBasicDTO {
    private Integer courseId;
    private String title;
    private String description;
    private String teacherName;
    private Integer totalLessons;
}