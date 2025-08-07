package com.example.courses.controller;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.LessonCreateRequest;
import com.example.courses.model.dto.request.LessonUpdateIsPublish;
import com.example.courses.model.dto.request.LessonUpdateRequest;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.LessonDetailResponse;
import com.example.courses.model.dto.response.LessonResponse;
import com.example.courses.service.LessonService;
import jakarta.validation.Valid;
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


    /**
     * Cập nhật thông tin bài học
     * Quyền: TEACHER (phải là người phụ trách khóa học) hoặc ADMIN
     */
    @PutMapping("{lessonId}")

    public ResponseEntity<DataResponse<LessonResponse>> updateLesson(
            @PathVariable Integer lessonId,
            @Valid @RequestBody LessonUpdateRequest request) throws NotFoundException, BadRequestException {

        LessonResponse lesson = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(DataResponse.success(lesson, "Cập nhật bài học thành công"));
    }

    /**
     * Cập nhật trạng thái hiển thị bài học (xuất bản/ẩn)
     * Quyền: TEACHER (phải là người phụ trách khóa học) hoặc ADMIN
     */
    @PutMapping("{lessonId}/publish")
    public ResponseEntity<DataResponse<LessonResponse>> updateLessonPublishStatus(
            @PathVariable Integer lessonId,
             @RequestBody LessonUpdateIsPublish isPublished) throws NotFoundException, BadRequestException {

        LessonResponse lesson = lessonService.updateLessonPublishStatus(lessonId, isPublished.getIsPublish());
        String message = isPublished.getIsPublish() ? "Xuất bản bài học thành công" : "Ẩn bài học thành công";
        return ResponseEntity.ok(DataResponse.success(lesson, message));
    }

    /**
     * Xóa bài học
     * Quyền: TEACHER (phải là người phụ trách khóa học) hoặc ADMIN
     */
    @DeleteMapping("{lessonId}")
    public ResponseEntity<DataResponse<Void>> deleteLesson(
            @PathVariable Integer lessonId) throws NotFoundException, BadRequestException {

        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(DataResponse.success(null, "Xóa bài học thành công"));
    }
}