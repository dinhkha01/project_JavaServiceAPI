package com.example.courses.model.dto.request;

import com.example.courses.model.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusUpdateRequest {

    @NotNull(message = "Trạng thái khóa học không được để trống")
    private CourseStatus status;
}