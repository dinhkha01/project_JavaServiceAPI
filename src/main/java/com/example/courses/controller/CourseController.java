package com.example.courses.controller;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.CourseCreateRequest;
import com.example.courses.model.dto.request.CourseUpdateRequest;
import com.example.courses.model.dto.request.CourseStatusUpdateRequest;
import com.example.courses.model.dto.request.LessonCreateRequest;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.CourseResponse;
import com.example.courses.model.dto.response.CourseDetailResponse;
import com.example.courses.model.dto.response.LessonResponse;
import com.example.courses.model.entity.CourseStatus;
import com.example.courses.service.CourseService;
import com.example.courses.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final LessonService lessonService;

    /**
     * GET /api/courses - Lấy danh sách tất cả khóa học với các tùy chọn lọc
     * Hỗ trợ các tham số:
     * - search: tìm kiếm theo từ khóa trong tiêu đề/mô tả
     * - teacher_id: lọc theo giảng viên
     * - status: lọc theo trạng thái khóa học
     * - keyword: tìm kiếm theo từ khóa (tương tự search)
     * Quyền: AUTHENTICATED
     */
    @GetMapping
    public ResponseEntity<DataResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String search,
            @RequestParam(name = "teacher_id", required = false) Integer teacherId
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            // Ưu tiên search parameter hơn keyword parameter
            String searchKeyword = search != null ? search : keyword;

            Page<CourseResponse> courses = courseService.getAllCoursesWithFilters(
                    pageable, status, searchKeyword, teacherId);

            return ResponseEntity.ok(DataResponse.success(courses, "Lấy danh sách khóa học thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy danh sách khóa học: " + e.getMessage()));
        }
    }

    /**
     * GET /api/courses/search - Endpoint riêng cho tìm kiếm khóa học
     * Quyền: AUTHENTICATED
     */
    @GetMapping("/search")
    public ResponseEntity<DataResponse<Page<CourseResponse>>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(name = "teacher_id", required = false) Integer teacherId
    ) {
        try {
            // Validate keyword không được rỗng
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(DataResponse.error("Từ khóa tìm kiếm không được để trống"));
            }

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CourseResponse> courses = courseService.getAllCoursesWithFilters(
                    pageable, status, keyword.trim(), teacherId);

            return ResponseEntity.ok(DataResponse.success(courses,
                    "Tìm kiếm khóa học với từ khóa '" + keyword + "' thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi tìm kiếm khóa học: " + e.getMessage()));
        }
    }

    /**
     * GET /api/courses/by-teacher/{teacherId} - Lấy danh sách khóa học theo giảng viên
     * Quyền: AUTHENTICATED
     */
    @GetMapping("/by-teacher/{teacherId}")
    public ResponseEntity<DataResponse<Page<CourseResponse>>> getCoursesByTeacher(
            @PathVariable Integer teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String keyword
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CourseResponse> courses = courseService.getAllCoursesWithFilters(
                    pageable, status, keyword, teacherId);

            return ResponseEntity.ok(DataResponse.success(courses,
                    "Lấy danh sách khóa học của giảng viên thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy danh sách khóa học theo giảng viên: " + e.getMessage()));
        }
    }

    /**
     * GET /api/courses/{course_id} - Lấy thông tin chi tiết một khóa học
     * Quyền: AUTHENTICATED
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<DataResponse<CourseDetailResponse>> getCourseDetail(
            @PathVariable Integer courseId
    ) {
        try {
            CourseDetailResponse course = courseService.getCourseDetail(courseId);
            return ResponseEntity.ok(DataResponse.success(course, "Lấy thông tin khóa học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy thông tin khóa học: " + e.getMessage()));
        }
    }

    /**
     * POST /api/courses - Tạo khóa học mới
     * Quyền: ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request
    ) {
        try {
            CourseResponse course = courseService.createCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DataResponse.success(course, "Tạo khóa học thành công"));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy giảng viên: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi tạo khóa học: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/courses/{course_id} - Cập nhật thông tin chi tiết khóa học
     * Quyền: ADMIN
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<CourseResponse>> updateCourse(
            @PathVariable Integer courseId,
            @Valid @RequestBody CourseUpdateRequest request
    ) {
        try {
            CourseResponse course = courseService.updateCourse(courseId, request);
            return ResponseEntity.ok(DataResponse.success(course, "Cập nhật khóa học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi cập nhật khóa học: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/courses/{course_id}/status - Cập nhật trạng thái khóa học
     * Quyền: ADMIN
     */
    @PutMapping("/{courseId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<CourseResponse>> updateCourseStatus(
            @PathVariable Integer courseId,
            @Valid @RequestBody CourseStatusUpdateRequest request
    ) {
        try {
            CourseResponse course = courseService.updateCourseStatus(courseId, request.getStatus());
            return ResponseEntity.ok(DataResponse.success(course, "Cập nhật trạng thái khóa học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Không thể cập nhật trạng thái: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi cập nhật trạng thái khóa học: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/courses/{course_id} - Xóa khóa học
     * Quyền: ADMIN
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deleteCourse(
            @PathVariable Integer courseId
    ) {
        try {
            courseService.deleteCourse(courseId);
            return ResponseEntity.ok(DataResponse.success(null, "Xóa khóa học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error("Không thể xóa khóa học: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi xóa khóa học: " + e.getMessage()));
        }
    }

    /**
     * GET /api/courses/{course_id}/lessons - Lấy danh sách tất cả bài học trong một khóa học
     * Quyền: AUTHENTICATED
     */
    @GetMapping("/{courseId}/lessons")
    public ResponseEntity<DataResponse<List<LessonResponse>>> getCourseLessons(
            @PathVariable Integer courseId
    ) {
        try {
            List<LessonResponse> lessons = lessonService.getPublishedLessonsByCourse(courseId);
            return ResponseEntity.ok(DataResponse.success(lessons, "Lấy danh sách bài học thành công"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy danh sách bài học: " + e.getMessage()));
        }
    }

    /**
     * POST /api/courses/{course_id}/lessons - Thêm bài học mới vào khóa học
     * Quyền: TEACHER (phải là người phụ trách khóa học) hoặc ADMIN
     */
    @PostMapping("/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<DataResponse<LessonResponse>> createLesson(
            @PathVariable Integer courseId,
            @Valid @RequestBody LessonCreateRequest request) throws BadRequestException, NotFoundException {

        LessonResponse lesson = lessonService.createLesson(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(lesson, "Tạo bài học thành công"));
    }
}