package com.example.courses.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lesson",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "order_index"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;

    @Column(name = "course_id", nullable = false)
    @NotNull(message = "Course ID không được để trống")
    private Integer courseId;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    private String title;

    @Column(name = "content_url")
    @NotBlank(message = "URL nội dung không được để trống")
    private String contentUrl;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "order_index")
    @NotNull(message = "Thứ tự bài học không được để trống")
    private Integer orderIndex;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgress> lessonProgresses;
}