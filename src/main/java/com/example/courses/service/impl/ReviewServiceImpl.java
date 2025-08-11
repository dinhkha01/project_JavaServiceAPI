package com.example.courses.service.impl;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.review.ReviewCreateRequest;
import com.example.courses.model.dto.request.review.ReviewUpdateRequest;
import com.example.courses.model.dto.response.review.ReviewListResponse;
import com.example.courses.model.dto.response.review.ReviewResponse;
import com.example.courses.model.entity.*;
import com.example.courses.repository.CourseRepository;
import com.example.courses.repository.EnrollmentRepository;
import com.example.courses.repository.ReviewRepository;
import com.example.courses.repository.UserRepository;
import com.example.courses.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getCourseReviews(Integer courseId, Pageable pageable) throws NotFoundException {
        // Kiểm tra khóa học tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }

        // Lấy danh sách reviews với phân trang
        Page<Review> reviewPage = reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable);

        // Convert sang DTO
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(this::convertToReviewResponse)
                .collect(Collectors.toList());

        // Tính điểm trung bình và tổng số reviews
        Double averageRating = reviewRepository.getAverageRatingByCourseId(courseId);
        Long totalReviews = reviewRepository.countByCourseId(courseId);

        return ReviewListResponse.builder()
                .reviews(reviewResponses)
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .currentPage(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .hasNext(reviewPage.hasNext())
                .hasPrevious(reviewPage.hasPrevious())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    @Override
    public ReviewResponse createReview(Integer courseId, ReviewCreateRequest request, String currentUsername)
            throws NotFoundException, BadRequestException {

        // Lấy thông tin user hiện tại
        User currentUser = getCurrentUser(currentUsername);

        // Kiểm tra khóa học tồn tại
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Kiểm tra user có quyền review không (phải là student và đã enroll khóa học)
        validateReviewPermission(currentUser, courseId);

        // Kiểm tra user đã review khóa học này chưa
        if (reviewRepository.existsByCourseIdAndStudentId(courseId, currentUser.getUserId())) {
            throw new BadRequestException("Bạn đã đánh giá khóa học này rồi");
        }

        // Tạo review mới
        Review review = new Review();
        review.setCourseId(courseId);
        review.setStudentId(currentUser.getUserId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCourse(course);
        review.setStudent(currentUser);

        Review savedReview = reviewRepository.save(review);
        log.info("Created new review for course {} by user {}", courseId, currentUsername);

        return convertToReviewResponse(savedReview);
    }

    @Override
    public ReviewResponse updateReview(Integer reviewId, ReviewUpdateRequest request, String currentUsername)
            throws NotFoundException, BadRequestException {

        // Lấy thông tin user hiện tại
        User currentUser = getCurrentUser(currentUsername);

        // Lấy review cần cập nhật
        Review review = reviewRepository.findByIdWithStudent(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Kiểm tra quyền cập nhật (owner hoặc admin)
        validateReviewOwnership(review, currentUser);

        // Cập nhật thông tin
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Updated review {} by user {}", reviewId, currentUsername);

        return convertToReviewResponse(updatedReview);
    }

    @Override
    public void deleteReview(Integer reviewId, String currentUsername)
            throws NotFoundException, BadRequestException {

        // Lấy thông tin user hiện tại
        User currentUser = getCurrentUser(currentUsername);

        // Lấy review cần xóa
        Review review = reviewRepository.findByIdWithStudent(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Kiểm tra quyền xóa (owner hoặc admin)
        validateReviewOwnership(review, currentUser);

        reviewRepository.delete(review);
        log.info("Deleted review {} by user {}", reviewId, currentUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Integer reviewId) throws NotFoundException {
        Review review = reviewRepository.findByIdWithStudent(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        return convertToReviewResponse(review);
    }

    /**
     * Lấy thông tin user hiện tại
     */
    private User getCurrentUser(String username) throws NotFoundException {
        if (username == null) {
            throw new RuntimeException("Không thể xác định người dùng hiện tại");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng hiện tại"));
    }

    /**
     * Kiểm tra quyền review (phải là student và đã enroll khóa học)
     */
    private void validateReviewPermission(User user, Integer courseId) throws BadRequestException {
        // Kiểm tra user có phải student không
        if (!user.getRole().equals(Role.ROLE_STUDENT)) {
            throw new BadRequestException("Chỉ sinh viên mới có thể đánh giá khóa học");
        }

        // Kiểm tra đã enroll khóa học chưa
        boolean hasEnrolled = enrollmentRepository.existsByStudentUserIdAndCourseCourseId(
                user.getUserId(), courseId);

        if (!hasEnrolled) {
            throw new BadRequestException("Bạn phải đăng ký khóa học trước khi có thể đánh giá");
        }
    }

    /**
     * Kiểm tra quyền sở hữu review (owner hoặc admin)
     */
    private void validateReviewOwnership(Review review, User currentUser) throws BadRequestException {
        boolean isOwner = Objects.equals(review.getStudentId(), currentUser.getUserId());
        boolean isAdmin = currentUser.getRole().equals(Role.ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            throw new BadRequestException("Bạn không có quyền thực hiện hành động này");
        }
    }

    /**
     * Convert Review entity sang ReviewResponse DTO
     */
    private ReviewResponse convertToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .comment(review.getComment())
                .studentId(review.getStudentId())
                .studentName(review.getStudent() != null ? review.getStudent().getFullName() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}