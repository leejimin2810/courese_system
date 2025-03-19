package com.example.coursesystem.controller;

import com.example.coursesystem.model.Course;
import com.example.coursesystem.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register/{courseId}/{email}")
    public ResponseEntity<?> registerCourse(@PathVariable Long courseId, @PathVariable String email) {
        try {
            List<Course> upcomingCourses = registrationService.registerCourse(email, courseId);
            return ResponseEntity.ok(upcomingCourses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/unregister/{courseId}/{email}")
    public ResponseEntity<?> unregisterCourse(@PathVariable Long courseId, @PathVariable String email) {
        try {
            registrationService.unregisterCourse(courseId, email);
            return ResponseEntity.ok("Hủy đăng ký thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}