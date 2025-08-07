package com.example.courses.controller;

import com.example.courses.model.dto.request.*;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.EnrollmentDetailResponseDTO;
import com.example.courses.model.dto.response.EnrollmentResponseDTO;
import com.example.courses.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * GET /api/enrollments
     * Lấy danh sách các khóa học sinh viên đã đăng ký
     */
    @GetMapping
    public ResponseEntity<DataResponse<Page<EnrollmentResponseDTO>>> getMyEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/enrollments - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        DataResponse<Page<EnrollmentResponseDTO>> response = enrollmentService
                .getMyEnrollments(page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/enrollments
     * Đăng ký một khóa học
     */
    @PostMapping
    public ResponseEntity<DataResponse<EnrollmentResponseDTO>> enrollCourse(
            @Valid @RequestBody EnrollmentRequestDTO request) {

        log.info("POST /api/enrollments - courseId: {}", request.getCourseId());

        DataResponse<EnrollmentResponseDTO> response = enrollmentService.enrollCourse(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/enrollments/{enrollment_id}
     * Lấy chi tiết thông tin đăng ký (tiến độ học) của mình
     */
    @GetMapping("/{enrollmentId}")
    public ResponseEntity<DataResponse<EnrollmentDetailResponseDTO>> getEnrollmentDetail(
            @PathVariable Integer enrollmentId) {

        log.info("GET /api/enrollments/{}", enrollmentId);

        try {
            DataResponse<EnrollmentDetailResponseDTO> response = enrollmentService
                    .getEnrollmentDetail(enrollmentId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error getting enrollment detail {}: {}", enrollmentId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    DataResponse.error(e.getMessage())
            );
        }
    }

    /**
     * PUT /api/enrollments/{enrollment_id}/complete_lesson/{lesson_id}
     * Cập nhật tiến độ học: đánh dấu một bài học đã hoàn thành
     */
    @PutMapping("/{enrollmentId}/complete_lesson/{lessonId}")
    public ResponseEntity<DataResponse<CompleteLessonResponseDTO>> completeLesson(
            @PathVariable Integer enrollmentId,
            @PathVariable Integer lessonId,
            @Valid @RequestBody(required = false) CompleteLessonRequestDTO request) {

        log.info("PUT /api/enrollments/{}/complete_lesson/{}", enrollmentId, lessonId);

        // Nếu không có request body, mặc định là hoàn thành bài học
        if (request == null) {
            request = new CompleteLessonRequestDTO(true);
        }

        try {
            DataResponse<CompleteLessonResponseDTO> response = enrollmentService
                    .completeLesson(enrollmentId, lessonId, request);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error completing lesson {} in enrollment {}: {}",
                    lessonId, enrollmentId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    DataResponse.error(e.getMessage())
            );
        }
    }

}