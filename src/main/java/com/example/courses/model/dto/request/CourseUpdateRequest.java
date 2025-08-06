package com.example.courses.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {

    @NotBlank(message = "Tiêu đề khóa học không được để trống")
    private String title;

    private String description;

    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    private BigDecimal price;

    private Integer durationHours;
}