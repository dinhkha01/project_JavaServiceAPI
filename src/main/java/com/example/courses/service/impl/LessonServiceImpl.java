package com.example.courses.service.impl;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.lesson.LessonCreateRequest;
import com.example.courses.model.dto.request.lesson.LessonUpdateRequest;
import com.example.courses.model.dto.response.lesson.LessonDetailResponse;
import com.example.courses.model.dto.response.lesson.LessonResponse;
import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.Lesson;
import com.example.courses.repository.CourseRepository;
import com.example.courses.repository.LessonRepository;
import com.example.courses.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    public LessonResponse createLesson(Integer courseId, LessonCreateRequest request) throws BadRequestException, NotFoundException {
        // Kiểm tra khóa học tồn tại
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Kiểm tra quyền: TEACHER phải là người phụ trách khóa học
        validateTeacherPermission(course);

        // Kiểm tra trùng orderIndex trong khóa học
        if (lessonRepository.existsByCourseIdAndOrderIndex(courseId, request.getOrderIndex())) {
            throw new BadRequestException("Thứ tự bài học đã tồn tại trong khóa học này");
        }

        // Tạo lesson mới
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setTextContent(request.getTextContent());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setIsPublished(false);
        lesson.setCourseId(courseId);

        Lesson savedLesson = lessonRepository.save(lesson);
        return convertToLessonResponse(savedLesson);
    }

    @Override
    public LessonResponse updateLesson(Integer lessonId, LessonUpdateRequest request) throws NotFoundException, BadRequestException {
        // Kiểm tra lesson tồn tại
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Lấy thông tin khóa học
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học"));

        // Kiểm tra quyền: TEACHER phải là người phụ trách khóa học
        validateTeacherPermission(course);

        // Kiểm tra trùng orderIndex nếu có thay đổi
        if (!lesson.getOrderIndex().equals(request.getOrderIndex()) &&
                lessonRepository.existsByCourseIdAndOrderIndex(lesson.getCourseId(), request.getOrderIndex())) {
            throw new BadRequestException("Thứ tự bài học đã tồn tại trong khóa học này");
        }

        // Cập nhật thông tin
        lesson.setTitle(request.getTitle());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setTextContent(request.getTextContent());
        lesson.setOrderIndex(request.getOrderIndex());

        Lesson savedLesson = lessonRepository.save(lesson);
        return convertToLessonResponse(savedLesson);
    }

    @Override
    public LessonResponse updateLessonPublishStatus(Integer lessonId, Boolean isPublished) throws NotFoundException, BadRequestException {
        // Kiểm tra lesson tồn tại
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Lấy thông tin khóa học
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học"));

        // Kiểm tra quyền: TEACHER phải là người phụ trách khóa học
        validateTeacherPermission(course);

        // Cập nhật trạng thái xuất bản
        lesson.setIsPublished(isPublished);

        Lesson savedLesson = lessonRepository.save(lesson);
        return convertToLessonResponse(savedLesson);
    }

    @Override
    public void deleteLesson(Integer lessonId) throws NotFoundException, BadRequestException {
        // Kiểm tra lesson tồn tại
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Lấy thông tin khóa học
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học"));

        // Kiểm tra quyền: TEACHER phải là người phụ trách khóa học
        validateTeacherPermission(course);

        // Kiểm tra có lesson progress không (học viên đã học)
        if (lesson.getLessonProgresses() != null && !lesson.getLessonProgresses().isEmpty()) {
            throw new BadRequestException("Không thể xóa bài học đã có học viên học");
        }

        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> getPublishedLessonsByCourse(Integer courseId) throws NotFoundException {
        // Kiểm tra khóa học tồn tại
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        // Lấy danh sách bài học đã xuất bản, sắp xếp theo orderIndex
        List<Lesson> publishedLessons = lessonRepository.findByCourseAndIsPublishedTrueOrderByOrderIndexAsc(course);

        return publishedLessons.stream()
                .map(this::convertToLessonResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailResponse getPublishedLessonDetail(Integer lessonId) throws NotFoundException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // Kiểm tra bài học đã được xuất bản chưa
        if (!Boolean.TRUE.equals(lesson.getIsPublished())) {
            throw new NotFoundException("Bài học chưa được xuất bản hoặc không tồn tại");
        }

        return convertToLessonDetailResponse(lesson);
    }

    /**
     * Kiểm tra quyền TEACHER cho khóa học
     * ADMIN có thể thao tác mọi khóa học
     * TEACHER chỉ được thao tác khóa học của mình
     */
    private void validateTeacherPermission(Course course) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Không có quyền truy cập");
        }

        // Lấy thông tin user hiện tại
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        // ADMIN có thể thao tác mọi khóa học
        if (isAdmin) {
            return;
        }

        // TEACHER chỉ được thao tác khóa học của mình
        if (isTeacher) {
            // Kiểm tra user hiện tại có phải là teacher của khóa học không
            if (course.getTeacher() != null && course.getTeacher().getUsername().equals(username)) {
                return;
            }
            throw new BadRequestException("Bạn chỉ có thể thao tác với khóa học của mình");
        }

        throw new BadRequestException("Không có quyền thực hiện thao tác này");
    }

    private LessonResponse convertToLessonResponse(Lesson lesson) {
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .contentUrl(lesson.getContentUrl())
                .textContent(lesson.getTextContent())
                .orderIndex(lesson.getOrderIndex())
                .isPublished(lesson.getIsPublished())
                .courseId(lesson.getCourseId())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private LessonDetailResponse convertToLessonDetailResponse(Lesson lesson) {
        return LessonDetailResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .contentUrl(lesson.getContentUrl())
                .textContent(lesson.getTextContent())
                .orderIndex(lesson.getOrderIndex())
                .isPublished(lesson.getIsPublished())
                .courseId(lesson.getCourse() != null ? lesson.getCourse().getCourseId() : null)
                .courseTitle(lesson.getCourse() != null ? lesson.getCourse().getTitle() : null)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}