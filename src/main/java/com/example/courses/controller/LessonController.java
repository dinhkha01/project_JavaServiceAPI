package com.example.courses.controller;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.LessonDetailResponse;
import com.example.courses.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    /**
     * GET /api/lessons/{lesson_id} - Lấy thông tin chi tiết một bài học
     * Quyền: AUTHENTICATED
     * Chỉ hiển thị bài học đã PUBLISHED
     */
    @GetMapping("/{lessonId}")
    public ResponseEntity<DataResponse<LessonDetailResponse>> getLessonDetail(
            @PathVariable Integer lessonId
    ) {
        try {
            LessonDetailResponse lesson = lessonService.getPublishedLessonDetail(lessonId);
            return ResponseEntity.ok(DataResponse.success(lesson, "Lấy thông tin bài học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy bài học với ID: " + lessonId + " hoặc bài học chưa được xuất bản"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy thông tin bài học: " + e.getMessage()));
        }
    }
}