package com.example.courses.service;

import com.example.courses.exception.BadRequestException;
import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.CourseCreateRequest;
import com.example.courses.model.dto.request.CourseUpdateRequest;
import com.example.courses.model.dto.response.CourseDetailResponse;
import com.example.courses.model.dto.response.CourseResponse;
import com.example.courses.model.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    Page<CourseResponse> getAllCourses(Pageable pageable, CourseStatus status, String keyword);

    // Thêm method mới để tìm kiếm với nhiều filter hơn
    Page<CourseResponse> getAllCoursesWithFilters(Pageable pageable, CourseStatus status, String keyword, Integer teacherId);

    CourseDetailResponse getCourseDetail(Integer courseId) throws NotFoundException;
    CourseResponse createCourse(CourseCreateRequest request) throws BadRequestException, NotFoundException;
    CourseResponse updateCourse(Integer courseId, CourseUpdateRequest request) throws NotFoundException, BadRequestException;
    CourseResponse updateCourseStatus(Integer courseId, CourseStatus status) throws NotFoundException, BadRequestException;
    void deleteCourse(Integer courseId) throws NotFoundException, BadRequestException;
}