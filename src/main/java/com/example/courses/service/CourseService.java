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
import org.springframework.security.core.Authentication;

public interface CourseService {
    /**
     * @deprecated Use getAllCoursesWithFilters instead
     */
    @Deprecated
    Page<CourseResponse> getAllCourses(Pageable pageable, CourseStatus status, String keyword);

    /**
     * Lấy danh sách khóa học với các filter
     * @param pageable thông tin phân trang
     * @param status trạng thái khóa học (có thể null để lấy tất cả theo quyền)
     * @param keyword từ khóa tìm kiếm
     * @param teacherId ID giảng viên (có thể null)
     * @return Page<CourseResponse>
     */
    Page<CourseResponse> getAllCoursesWithFilters(Pageable pageable, CourseStatus status, String keyword, Integer teacherId);

    /**
     * Lấy danh sách khóa học với role-based filtering
     * @param pageable thông tin phân trang
     * @param status trạng thái khóa học
     * @param keyword từ khóa tìm kiếm
     * @param teacherId ID giảng viên
     * @param authentication thông tin xác thực để check role
     * @return Page<CourseResponse>
     */
    Page<CourseResponse> getAllCoursesWithRoleBasedFilters(
            Pageable pageable,
            CourseStatus status,
            String keyword,
            Integer teacherId,
            Authentication authentication
    );

    CourseDetailResponse getCourseDetail(Integer courseId) throws NotFoundException;
    CourseResponse createCourse(CourseCreateRequest request) throws BadRequestException, NotFoundException;
    CourseResponse updateCourse(Integer courseId, CourseUpdateRequest request) throws NotFoundException, BadRequestException;
    CourseResponse updateCourseStatus(Integer courseId, CourseStatus status) throws NotFoundException, BadRequestException;
    void deleteCourse(Integer courseId) throws NotFoundException, BadRequestException;
}