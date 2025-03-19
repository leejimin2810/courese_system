package com.example.coursesystem.controller;

import com.example.coursesystem.model.Course;
import com.example.coursesystem.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        course1 = Course.builder()
                .id(1L)
                .name("Học làm giàu trong 1 ngày")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(5))
                .price(100000L)
                .build();

        course2 = Course.builder()
                .id(2L)
                .name("Đào tạo văn hóa trước khi sang Cam")
                .startTime(LocalDateTime.now().plusDays(5))
                .endTime(LocalDateTime.now().plusDays(10))
                .price(200000L)
                .build();
    }

    @Test
    void testRegisterCourse_Success() {
        Long courseId = 1L;
        String email = "leejimin@gmail.com";
        List<Course> courses = Arrays.asList(course1, course2);

        when(registrationService.registerCourse(email, courseId)).thenReturn(courses);

        ResponseEntity<?> response = registrationController.registerCourse(courseId, email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courses, response.getBody());
        verify(registrationService, times(1)).registerCourse(email, courseId);
    }

    @Test
    void testRegisterCourse_Exception() {
        Long courseId = 1L;
        String email = "leejimin@gmail.com";
        String errorMessage = "Không thể đăng ký khóa học";

        when(registrationService.registerCourse(email, courseId))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<?> response = registrationController.registerCourse(courseId, email);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void testUnregisterCourse_Success() {
        Long courseId = 1L;
        String email = "leejimin@gmail.com";

        when(registrationService.unregisterCourse(courseId, email)).thenReturn(true);

        ResponseEntity<?> response = registrationController.unregisterCourse(courseId, email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hủy đăng ký thành công", response.getBody());
    }

    @Test
    void testUnregisterCourse_Exception() {
        Long courseId = 1L;
        String email = "leejimin@gmail.com";
        String errorMessage = "Không thể hủy đăng ký";

        when(registrationService.unregisterCourse(courseId, email))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseEntity<?> response = registrationController.unregisterCourse(courseId, email);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}