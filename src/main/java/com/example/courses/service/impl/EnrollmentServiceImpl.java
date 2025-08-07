package com.example.courses.service.impl;

import com.example.courses.model.dto.request.*;
import com.example.courses.model.dto.response.*;
import com.example.courses.model.entity.*;
import com.example.courses.repository.*;
import com.example.courses.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public DataResponse<Page<EnrollmentResponseDTO>> getMyEnrollments(
            int page, int size, String sortBy, String sortDir) {

        Integer currentUserId = getCurrentUserId();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUserId, pageable);
        Page<EnrollmentResponseDTO> response = enrollments.map(this::convertToResponseDTO);

        log.info("Retrieved {} enrollments for user {}", response.getTotalElements(), currentUserId);
        return DataResponse.success(response, "Lấy danh sách đăng ký thành công");
    }

    @Override
    @Transactional
    public DataResponse<EnrollmentResponseDTO> enrollCourse(EnrollmentRequestDTO request) {
        Integer currentUserId = getCurrentUserId();

        // Kiểm tra xem đã đăng ký khóa học này chưa
        if (enrollmentRepository.existsByStudentUserIdAndCourseCourseId(currentUserId, request.getCourseId())) {
            return DataResponse.error("Bạn đã đăng ký khóa học này rồi");
        }

        // Lấy thông tin course và user
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        if (!CourseStatus.PUBLISHED.equals(course.getStatus())) {
            return DataResponse.error("Khóa học chưa được xuất bản");
        }

        User student = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setProgressPercentage(BigDecimal.ZERO);

        enrollment = enrollmentRepository.save(enrollment);

        // Tạo lesson progress cho tất cả lessons đã publish
        lessonProgressRepository.createProgressForAllLessons(
                enrollment.getEnrollmentId(),
                course.getCourseId()
        );

        log.info("User {} enrolled in course {} - enrollment ID: {}",
                currentUserId, request.getCourseId(), enrollment.getEnrollmentId());

        return DataResponse.success(
                convertToResponseDTO(enrollment),
                "Đăng ký khóa học thành công"
        );
    }

    @Override
    public DataResponse<EnrollmentDetailResponseDTO> getEnrollmentDetail(Integer enrollmentId) {
        Integer currentUserId = getCurrentUserId();

        Enrollment enrollment = enrollmentRepository.findByIdAndStudentId(enrollmentId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký hoặc bạn không có quyền truy cập"));

        EnrollmentDetailResponseDTO response = convertToDetailResponseDTO(enrollment);

        log.info("Retrieved enrollment detail {} for user {}", enrollmentId, currentUserId);
        return DataResponse.success(response, "Lấy chi tiết đăng ký thành công");
    }

    @Override
    @Transactional
    public DataResponse<CompleteLessonResponseDTO> completeLesson(
            Integer enrollmentId, Integer lessonId, CompleteLessonRequestDTO request) {

        Integer currentUserId = getCurrentUserId();

        // Kiểm tra quyền truy cập enrollment
        Enrollment enrollment = enrollmentRepository.findByIdAndStudentId(enrollmentId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký hoặc bạn không có quyền truy cập"));

        // Tìm lesson progress
        LessonProgress lessonProgress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tiến độ bài học"));

        // Cập nhật trạng thái hoàn thành
        LocalDateTime completedAt = request.getIsCompleted() ? LocalDateTime.now() : null;

        lessonProgress.setIsCompleted(request.getIsCompleted());
        lessonProgress.setCompletedAt(completedAt);
        lessonProgress.setLastAccessedAt(LocalDateTime.now());

        lessonProgress = lessonProgressRepository.save(lessonProgress);

        // Cập nhật progress percentage của enrollment
        BigDecimal newProgressPercentage = calculateProgressPercentage(enrollmentId);
        enrollment.setProgressPercentage(newProgressPercentage);

        // Kiểm tra xem đã hoàn thành khóa học chưa
        if (newProgressPercentage.compareTo(new BigDecimal("100")) == 0 &&
                !EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletionDate(LocalDateTime.now());
            log.info("User {} completed course in enrollment {}", currentUserId, enrollmentId);
        }

        enrollmentRepository.save(enrollment);

        log.info("User {} {} lesson {} in enrollment {}",
                currentUserId,
                request.getIsCompleted() ? "completed" : "uncompleted",
                lessonId,
                enrollmentId);

        CompleteLessonResponseDTO response = new CompleteLessonResponseDTO(
                lessonProgress.getProgressId(),
                enrollmentId,
                lessonId,
                lessonProgress.getLesson().getTitle(),
                lessonProgress.getIsCompleted(),
                newProgressPercentage,
                lessonProgress.getCompletedAt(),
                lessonProgress.getLastAccessedAt()
        );

        return DataResponse.success(response, "Cập nhật tiến độ học thành công");
    }


    /**
     * Tính toán progress percentage
     */
    private BigDecimal calculateProgressPercentage(Integer enrollmentId) {
        long completedLessons = lessonProgressRepository.countByEnrollmentIdAndIsCompletedTrue(enrollmentId);
        long totalLessons = lessonProgressRepository.countByEnrollmentId(enrollmentId);

        if (totalLessons == 0) {
            return BigDecimal.ZERO;
        }

        double percentage = (double) completedLessons / totalLessons * 100;
        return BigDecimal.valueOf(Math.round(percentage * 100) / 100.0);
    }

    /**
     * Lấy ID của user hiện tại từ SecurityContext
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Không tìm thấy thông tin xác thực");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return user.getUserId();
    }

    /**
     * Convert Enrollment to EnrollmentResponseDTO
     */
    private EnrollmentResponseDTO convertToResponseDTO(Enrollment enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getEnrollmentId(),
                enrollment.getStudent().getUserId(),
                enrollment.getStudent().getFullName(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getDescription(),
                enrollment.getCourse().getTeacher().getFullName(),
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate()
        );
    }

    /**
     * Convert Enrollment to EnrollmentDetailResponseDTO
     */
    private EnrollmentDetailResponseDTO convertToDetailResponseDTO(Enrollment enrollment) {
        CourseBasicDTO courseDTO = new CourseBasicDTO(
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getDescription(),
                enrollment.getCourse().getTeacher().getFullName(),
                enrollment.getCourse().getLessons() != null ? enrollment.getCourse().getLessons().size() : 0
        );

        List<LessonProgressDTO> lessonProgressDTOs = lessonProgressRepository
                .findByEnrollmentIdOrderByLessonOrderIndex(enrollment.getEnrollmentId())
                .stream()
                .map(lp -> new LessonProgressDTO(
                        lp.getProgressId(),
                        lp.getLessonId(),
                        lp.getLesson().getTitle(),
                        lp.getLesson().getOrderIndex(),
                        lp.getIsCompleted(),
                        lp.getCompletedAt(),
                        lp.getLastAccessedAt()
                ))
                .collect(Collectors.toList());

        return new EnrollmentDetailResponseDTO(
                enrollment.getEnrollmentId(),
                enrollment.getStudent().getUserId(),
                enrollment.getStudent().getFullName(),
                courseDTO,
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate(),
                lessonProgressDTOs
        );
    }
}