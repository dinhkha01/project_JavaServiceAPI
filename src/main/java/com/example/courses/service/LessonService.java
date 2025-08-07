package com.example.courses.service;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.LessonCreateRequest;
import com.example.courses.model.dto.request.LessonUpdateRequest;
import com.example.courses.model.dto.response.LessonDetailResponse;
import com.example.courses.model.dto.response.LessonResponse;

import java.util.List;

public interface LessonService {
    LessonResponse createLesson(Integer courseId, LessonCreateRequest request) throws BadRequestException, NotFoundException;
    LessonResponse updateLesson(Integer lessonId, LessonUpdateRequest request) throws NotFoundException, BadRequestException;
    LessonResponse updateLessonPublishStatus(Integer lessonId, Boolean isPublished) throws NotFoundException, BadRequestException;
    void deleteLesson(Integer lessonId) throws NotFoundException, BadRequestException;
    List<LessonResponse> getPublishedLessonsByCourse(Integer courseId) throws NotFoundException;
    LessonDetailResponse getPublishedLessonDetail(Integer lessonId) throws NotFoundException;
}