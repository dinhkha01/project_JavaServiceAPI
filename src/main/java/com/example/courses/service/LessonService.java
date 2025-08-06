package com.example.courses.service;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.response.LessonDetailResponse;
import com.example.courses.model.dto.response.LessonResponse;

import java.util.List;

public interface LessonService {
    List<LessonResponse> getPublishedLessonsByCourse(Integer courseId) throws NotFoundException;
    LessonDetailResponse getPublishedLessonDetail(Integer lessonId) throws NotFoundException;
}