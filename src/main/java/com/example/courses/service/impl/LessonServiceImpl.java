package com.example.courses.service.impl;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.response.LessonDetailResponse;
import com.example.courses.model.dto.response.LessonResponse;
import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.Lesson;
import com.example.courses.repository.CourseRepository;
import com.example.courses.repository.LessonRepository;
import com.example.courses.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

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

    private LessonResponse convertToLessonResponse(Lesson lesson) {
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .contentUrl(lesson.getContentUrl())
                .orderIndex(lesson.getOrderIndex())
                .isPublished(lesson.getIsPublished())
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