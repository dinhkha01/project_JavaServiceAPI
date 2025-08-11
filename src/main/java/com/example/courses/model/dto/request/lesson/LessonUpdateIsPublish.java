package com.example.courses.model.dto.request.lesson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateIsPublish {

    private Boolean isPublish; // true: công khai, false: không công khai
}
