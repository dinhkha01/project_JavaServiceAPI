package com.example.courses.service;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.review.ReviewCreateRequest;
import com.example.courses.model.dto.request.review.ReviewUpdateRequest;
import com.example.courses.model.dto.response.review.ReviewListResponse;
import com.example.courses.model.dto.response.review.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Lấy danh sách reviews của một khóa học
     */
    ReviewListResponse getCourseReviews(Integer courseId, Pageable pageable) throws NotFoundException;

    /**
     * Tạo review mới cho khóa học
     */
    ReviewResponse createReview(Integer courseId, ReviewCreateRequest request, String currentUsername)
            throws NotFoundException, BadRequestException;

    /**
     * Cập nhật review
     */
    ReviewResponse updateReview(Integer reviewId, ReviewUpdateRequest request, String currentUsername)
            throws NotFoundException, BadRequestException;

    /**
     * Xóa review
     */
    void deleteReview(Integer reviewId, String currentUsername)
            throws NotFoundException, BadRequestException;

    /**
     * Lấy chi tiết một review
     */
    ReviewResponse getReviewById(Integer reviewId) throws NotFoundException;
}