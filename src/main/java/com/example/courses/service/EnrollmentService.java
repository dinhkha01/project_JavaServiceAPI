package com.example.courses.service;

import com.example.courses.model.dto.request.*;
import com.example.courses.model.dto.request.LessonProgress.CompleteLessonRequestDTO;
import com.example.courses.model.dto.response.course.CompleteLessonResponseDTO;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.enrollment.EnrollmentDetailResponseDTO;
import com.example.courses.model.dto.response.enrollment.EnrollmentResponseDTO;
import org.springframework.data.domain.Page;

/**
 * Interface cho EnrollmentService
 * Định nghĩa các phương thức xử lý enrollment logic
 */
public interface EnrollmentService {

    /**
     * Lấy danh sách enrollments của sinh viên hiện tại
     *
     * @param page trang hiện tại
     * @param size kích thước trang
     * @param sortBy trường sắp xếp
     * @param sortDir hướng sắp xếp (asc/desc)
     * @return danh sách enrollments với pagination
     */
    DataResponse<Page<EnrollmentResponseDTO>> getMyEnrollments(
            int page, int size, String sortBy, String sortDir);

    /**
     * Đăng ký khóa học mới
     *
     * @param request thông tin đăng ký khóa học
     * @return thông tin enrollment đã tạo
     */
    DataResponse<EnrollmentResponseDTO> enrollCourse(EnrollmentRequestDTO request);

    /**
     * Lấy chi tiết thông tin đăng ký và tiến độ học
     *
     * @param enrollmentId ID của enrollment
     * @return chi tiết enrollment với danh sách lesson progress
     */
    DataResponse<EnrollmentDetailResponseDTO> getEnrollmentDetail(Integer enrollmentId);

    /**
     * Cập nhật tiến độ học - đánh dấu bài học hoàn thành
     *
     * @param enrollmentId ID của enrollment
     * @param lessonId ID của lesson
     * @param request thông tin trạng thái hoàn thành
     * @return thông tin lesson progress đã cập nhật
     */
    DataResponse<CompleteLessonResponseDTO> completeLesson(
            Integer enrollmentId, Integer lessonId, CompleteLessonRequestDTO request);


}