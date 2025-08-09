package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponse {
    private List<ReviewResponse> reviews;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private Double averageRating;
    private Long totalReviews;
}