package com.example.courses.controller;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.ReviewCreateRequest;
import com.example.courses.model.dto.request.ReviewUpdateRequest;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.ReviewListResponse;
import com.example.courses.model.dto.response.ReviewResponse;
import com.example.courses.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * GET /api/courses/{course_id}/reviews - Lấy danh sách đánh giá/bình luận về khóa học
     * Quyền: AUTHENTICATED - Tất cả user đã đăng nhập đều có thể xem reviews
     */
    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<DataResponse<ReviewListResponse>> getCourseReviews(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            // Validate page parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(DataResponse.error("Số trang không được nhỏ hơn 0"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(DataResponse.error("Kích thước trang phải từ 1-100"));
            }

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            ReviewListResponse reviews = reviewService.getCourseReviews(courseId, pageable);

            return ResponseEntity.ok(DataResponse.success(reviews,
                    "Lấy danh sách đánh giá khóa học thành công"));

        } catch (NotFoundException e) {
            log.warn("Course not found when getting reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy khóa học với ID: " + courseId));
        } catch (Exception e) {
            log.error("Error getting course reviews for course {}: ", courseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy danh sách đánh giá: " + e.getMessage()));
        }
    }

    /**
     * POST /api/courses/{course_id}/reviews - Sinh viên gửi đánh giá/bình luận về khóa học đã học
     * Quyền: STUDENT - Chỉ sinh viên mới có thể tạo review (đã enroll khóa học)
     */
    @PostMapping("/courses/{courseId}/reviews")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DataResponse<ReviewResponse>> createCourseReview(
            @PathVariable Integer courseId,
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication
    ) {
        try {
            String currentUsername = authentication.getName();
            if (currentUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DataResponse.error("Không thể xác định người dùng hiện tại"));
            }

            ReviewResponse review = reviewService.createReview(courseId, request, currentUsername);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DataResponse.success(review, "Tạo đánh giá khóa học thành công"));

        } catch (NotFoundException e) {
            log.warn("Resource not found when creating review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("Bad request when creating review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating review for course {} by user {}: ",
                    courseId, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi tạo đánh giá: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reviews/{review_id} - Lấy thông tin chi tiết một đánh giá
     * Quyền: AUTHENTICATED - Tất cả user đã đăng nhập đều có thể xem chi tiết review
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<DataResponse<ReviewResponse>> getReviewById(
            @PathVariable Integer reviewId
    ) {
        try {
            ReviewResponse review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(DataResponse.success(review,
                    "Lấy thông tin đánh giá thành công"));

        } catch (NotFoundException e) {
            log.warn("Review not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error("Không tìm thấy đánh giá với ID: " + reviewId));
        } catch (Exception e) {
            log.error("Error getting review {}: ", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi lấy thông tin đánh giá: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/reviews/{review_id} - Cập nhật đánh giá/bình luận
     * Quyền: OWNER_OR_ADMIN - Chỉ người tạo review hoặc admin mới có thể cập nhật
     * Logic phân quyền được xử lý trong service layer
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<DataResponse<ReviewResponse>> updateReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String currentUsername = authentication.getName();
            if (currentUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DataResponse.error("Không thể xác định người dùng hiện tại"));
            }

            ReviewResponse updatedReview = reviewService.updateReview(reviewId, request, currentUsername);

            return ResponseEntity.ok(DataResponse.success(updatedReview,
                    "Cập nhật đánh giá thành công"));

        } catch (NotFoundException e) {
            log.warn("Resource not found when updating review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("Bad request when updating review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating review {} by user {}: ",
                    reviewId, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi cập nhật đánh giá: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/reviews/{review_id} - Xóa đánh giá/bình luận
     * Quyền: OWNER_OR_ADMIN - Chỉ người tạo review hoặc admin mới có thể xóa
     * Logic phân quyền được xử lý trong service layer
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<DataResponse<Void>> deleteReview(
            @PathVariable Integer reviewId,
            Authentication authentication
    ) {
        try {
            String currentUsername = authentication.getName();
            if (currentUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DataResponse.error("Không thể xác định người dùng hiện tại"));
            }

            reviewService.deleteReview(reviewId, currentUsername);

            return ResponseEntity.ok(DataResponse.success(null,
                    "Xóa đánh giá thành công"));

        } catch (NotFoundException e) {
            log.warn("Resource not found when deleting review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DataResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("Bad request when deleting review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DataResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting review {} by user {}: ",
                    reviewId, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DataResponse.error("Lỗi khi xóa đánh giá: " + e.getMessage()));
        }
    }
}