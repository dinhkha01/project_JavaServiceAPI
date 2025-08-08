package com.example.courses.controller;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.response.StudentProgressResponse;
import com.example.courses.model.dto.response.TeacherCoursesOverviewResponse;
import com.example.courses.model.dto.response.TopCourseResponse;
import com.example.courses.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

//    @GetMapping("/top_courses")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Page<TopCourseResponse>> getTopCourses(Pageable pageable) {
//
//        Page<TopCourseResponse> topCourses = reportService.getTopCourses(pageable);
//        return ResponseEntity.ok(topCourses);
//    }

    @GetMapping("/student_progress/{student_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProgressResponse> getStudentProgress(
            @PathVariable("student_id") Integer studentId) throws NotFoundException {

        StudentProgressResponse progress = reportService.getStudentProgress(studentId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/teacher_courses_overview/{teacher_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherCoursesOverviewResponse> getTeacherCoursesOverview(
            @PathVariable("teacher_id") Integer teacherId) throws NotFoundException {

        TeacherCoursesOverviewResponse overview = reportService.getTeacherCoursesOverview(teacherId);
        return ResponseEntity.ok(overview);
    }
}