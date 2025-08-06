package com.example.courses.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {

    @NotBlank(message = "Tiêu đề khóa học không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Teacher ID không được để trống")
    private int teacherId;

    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    private BigDecimal price = BigDecimal.ZERO;

    private int durationHours;
}