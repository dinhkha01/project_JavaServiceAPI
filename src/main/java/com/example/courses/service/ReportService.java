package com.example.courses.service;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.response.StudentProgressResponse;
import com.example.courses.model.dto.response.TeacherCoursesOverviewResponse;
import com.example.courses.model.dto.response.TopCourseResponse;
import com.example.courses.model.entity.Course;
import com.example.courses.model.entity.Role;
import com.example.courses.model.entity.User;
import com.example.courses.repository.CourseRepository;
import com.example.courses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách các khóa học phổ biến nhất theo số lượt đăng ký
     */
    public Page<Course> getTopCourses(Pageable pageable) {
        return courseRepository.findTopCoursesByEnrollments(pageable);
    }

    /**
     * Thống kê tiến độ học của một sinh viên cụ thể
     */
    public StudentProgressResponse getStudentProgress(Integer studentId) throws NotFoundException {
        // Kiểm tra sinh viên có tồn tại không và có role STUDENT
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sinh viên với ID: " + studentId));

        if (!Role.ROLE_STUDENT.equals(student.getRole())) {
            throw new NotFoundException("User với ID " + studentId + " không phải là sinh viên");
        }

        // Lấy thông tin tổng quan
        StudentProgressResponse response = courseRepository.getStudentProgressReport(studentId);

        // Nếu không có dữ liệu từ query (sinh viên chưa đăng ký khóa học nào)
        if (response == null) {
            response = StudentProgressResponse.builder()
                    .studentId(student.getUserId())
                    .studentName(student.getFullName())
                    .studentEmail(student.getEmail())
                    .totalCoursesEnrolled(0)
                    .completedCourses(0)
                    .inProgressCourses(0)
                    .overallProgressPercentage(0.0)
                    .courseProgress(null) // Không cần chi tiết
                    .lastActivityAt(null)
                    .build();
        }

        return response;
    }

    /**
     * Thống kê tổng quan về các khóa học của một giảng viên
     */
    public TeacherCoursesOverviewResponse getTeacherCoursesOverview(Integer teacherId) throws NotFoundException {
        // Kiểm tra giảng viên có tồn tại không và có role TEACHER
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giảng viên với ID: " + teacherId));

        if (!Role.ROLE_TEACHER.equals(teacher.getRole())) {
            throw new NotFoundException("User với ID " + teacherId + " không phải là giảng viên");
        }

        // Lấy thông tin tổng quan
        TeacherCoursesOverviewResponse response = courseRepository.getTeacherCoursesOverview(teacherId);

        // Nếu không có dữ liệu từ query (giảng viên chưa tạo khóa học nào)
        if (response == null) {
            response = TeacherCoursesOverviewResponse.builder()
                    .teacherId(teacher.getUserId())
                    .teacherName(teacher.getFullName())
                    .teacherEmail(teacher.getEmail())
                    .totalCourses(0)
                    .publishedCourses(0)
                    .draftCourses(0)
                    .archivedCourses(0)
                    .totalEnrollments(0)
                    .averageRating(0.0)
                    .totalRevenue(java.math.BigDecimal.ZERO)
                    .courses(null) // Không cần chi tiết từng khóa học
                    .enrollmentsByMonth(null) // Không cần thống kê theo tháng
                    .createdAt(teacher.getCreatedAt())
                    .build();
        }

        return response;
    }
}