package com.example.courses.service.impl;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.CourseCreateRequest;
import com.example.courses.model.dto.request.CourseUpdateRequest;
import com.example.courses.model.dto.response.CourseDetailResponse;
import com.example.courses.model.dto.response.CourseResponse;
import com.example.courses.model.dto.response.LessonResponse;
import com.example.courses.model.dto.response.ReviewResponse;
import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.CourseStatus;
import com.example.courses.model.entity.User;
import com.example.courses.repository.CourseRepository;
import com.example.courses.repository.UserRepository;
import com.example.courses.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable, CourseStatus status, String keyword) {
        Specification<Course> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo trạng thái nếu có
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Tìm kiếm theo từ khóa trong tiêu đề hoặc mô tả
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(this::convertToCourseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Integer courseId) throws NotFoundException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        return convertToCourseDetailResponse(course);
    }

    @Override
    public CourseResponse createCourse(CourseCreateRequest request) throws BadRequestException, NotFoundException {
        // Kiểm tra giảng viên tồn tại
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giảng viên với ID: " + request.getTeacherId()));
        System.out.println("Teacher found: " + teacher.getRole());
//         Kiểm tra role của user có phải là TEACHER không
        if (!teacher.getRole().name().equals("ROLE_TEACHER")) {
            throw new BadRequestException("User không có quyền làm giảng viên");
        }

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setTeacherId(request.getTeacherId());
        course.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        course.setDurationHours(request.getDurationHours());
        course.setStatus(CourseStatus.DRAFT);
        course.setTeacher(teacher);

        Course savedCourse = courseRepository.save(course);
        return convertToCourseResponse(savedCourse);
    }

    @Override
    public CourseResponse updateCourse(Integer courseId, CourseUpdateRequest request)
            throws NotFoundException, BadRequestException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setDurationHours(request.getDurationHours());

        Course savedCourse = courseRepository.save(course);
        return convertToCourseResponse(savedCourse);
    }

    @Override
    public CourseResponse updateCourseStatus(Integer courseId, CourseStatus status)
            throws NotFoundException, BadRequestException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Kiểm tra logic chuyển trạng thái
        if (status == CourseStatus.PUBLISHED) {
            // Kiểm tra khóa học có ít nhất 1 bài học đã xuất bản
            long publishedLessonsCount = course.getLessons() != null ?
                    course.getLessons().stream().filter(lesson -> Boolean.TRUE.equals(lesson.getIsPublished())).count() : 0;

            if (publishedLessonsCount == 0) {
                throw new BadRequestException("Không thể xuất bản khóa học khi chưa có bài học nào được xuất bản");
            }
        }

        course.setStatus(status);
        Course savedCourse = courseRepository.save(course);
        return convertToCourseResponse(savedCourse);
    }

    @Override
    public void deleteCourse(Integer courseId) throws NotFoundException, BadRequestException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Kiểm tra có enrollments không
        if (course.getEnrollments() != null && !course.getEnrollments().isEmpty()) {
            throw new BadRequestException("Không thể xóa khóa học đã có học viên đăng ký");
        }

        courseRepository.delete(course);
    }

    private CourseResponse convertToCourseResponse(Course course) {
        // Tính toán thống kê
        int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;
        int publishedLessons = course.getLessons() != null ?
                (int) course.getLessons().stream().filter(lesson -> Boolean.TRUE.equals(lesson.getIsPublished())).count() : 0;

        double averageRating = 0.0;
        if (course.getReviews() != null && !course.getReviews().isEmpty()) {
            averageRating = course.getReviews().stream()
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
        }

        int totalEnrollments = course.getEnrollments() != null ? course.getEnrollments().size() : 0;

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .teacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : null)
                .price(course.getPrice())
                .durationHours(course.getDurationHours())
                .status(course.getStatus())
                .totalLessons(totalLessons)
                .publishedLessons(publishedLessons)
                .averageRating(averageRating)
                .totalEnrollments(totalEnrollments)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private CourseDetailResponse convertToCourseDetailResponse(Course course) {
        // Tính toán thống kê
        int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;
        int publishedLessons = course.getLessons() != null ?
                (int) course.getLessons().stream().filter(lesson -> Boolean.TRUE.equals(lesson.getIsPublished())).count() : 0;

        double averageRating = 0.0;
        if (course.getReviews() != null && !course.getReviews().isEmpty()) {
            averageRating = course.getReviews().stream()
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
        }

        int totalEnrollments = course.getEnrollments() != null ? course.getEnrollments().size() : 0;

        // Convert published lessons
        List<LessonResponse> publishedLessonResponses = course.getLessons() != null ?
                course.getLessons().stream()
                        .filter(lesson -> Boolean.TRUE.equals(lesson.getIsPublished()))
                        .map(lesson -> LessonResponse.builder()
                                .lessonId(lesson.getLessonId())
                                .title(lesson.getTitle())
                                .contentUrl(lesson.getContentUrl())
                                .orderIndex(lesson.getOrderIndex())
                                .isPublished(lesson.getIsPublished())
                                .createdAt(lesson.getCreatedAt())
                                .updatedAt(lesson.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        // Convert reviews (lấy 10 review mới nhất)
        List<ReviewResponse> reviewResponses = course.getReviews() != null ?
                course.getReviews().stream()
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .limit(10)
                        .map(review -> ReviewResponse.builder()
                                .reviewId(review.getReviewId())
                                .rating(review.getRating())
                                .comment(review.getComment())
                                .studentId(review.getStudentId())
                                .studentName(review.getStudent() != null ? review.getStudent().getFullName() : null)
                                .createdAt(review.getCreatedAt())
                                .updatedAt(review.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        return CourseDetailResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .teacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : null)
                .teacherEmail(course.getTeacher() != null ? course.getTeacher().getEmail() : null)
                .price(course.getPrice())
                .durationHours(course.getDurationHours())
                .status(course.getStatus())
                .totalLessons(totalLessons)
                .publishedLessons(publishedLessons)
                .averageRating(averageRating)
                .totalEnrollments(totalEnrollments)
                .publishedLessonss(publishedLessonResponses)
                .reviews(reviewResponses)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}