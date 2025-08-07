package com.example.courses.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateRequest {

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    private String title;

    @NotBlank(message = "URL nội dung không được để trống")
    private String contentUrl;

    private String textContent;

    @NotNull(message = "Thứ tự bài học không được để trống")
    private Integer orderIndex;
}