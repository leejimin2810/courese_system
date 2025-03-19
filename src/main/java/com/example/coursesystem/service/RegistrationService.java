package com.example.coursesystem.service;

import com.example.coursesystem.model.Course;
import com.example.coursesystem.model.Registration;
import com.example.coursesystem.model.Student;
import com.example.coursesystem.repository.CourseRepository;
import com.example.coursesystem.repository.RegistrationRepository;
import com.example.coursesystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public RegistrationService(RegistrationRepository registrationRepository, CourseRepository courseRepository, StudentRepository studentRepository) {
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public List<Course> registerCourse(String email, Long courseId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học viên"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học"));

        if (course.hasStarted()) {
            throw new IllegalArgumentException("Không thể đăng ký khóa học đã bắt đầu");
        }

        boolean alreadyRegistered = registrationRepository
                .findByStudentIdAndCourseId(student.getId(), courseId)
                .isPresent();

        if (alreadyRegistered) {
            throw new IllegalArgumentException("Đã đăng ký khóa học này rồi");
        }

        int ongoingCoursesCount = registrationRepository
                .countOngoingCoursesByStudentId(student.getId(), LocalDateTime.now());

        Long finalPrice = course.getPrice();
        if (ongoingCoursesCount >= 2) {
            finalPrice = finalPrice * 75 / 100; // Giảm 25%
        }

        Registration registration = Registration.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .price(finalPrice)
                .registeredDate(LocalDateTime.now())
                .build();

        registrationRepository.save(registration);

        return getUpcomingRegisteredCourses(student.getId());
    }
    @Transactional
    public boolean unregisterCourse(Long courseId, String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học viên"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học"));

        if (course.hasStarted()) {
            throw new IllegalArgumentException("Không thể hủy đăng ký khóa học đã bắt đầu");
        }

        Registration registration = registrationRepository
                .findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký khóa học"));

        registrationRepository.delete(registration);
        return true;
    }

    private List<Course> getUpcomingRegisteredCourses(Long studentId) {
        List<Registration> upcomingRegistrations = registrationRepository
                .findUpcomingRegistrationsByStudentId(studentId, LocalDateTime.now());

        List<Course> courses = new ArrayList<>();
        for (Registration reg : upcomingRegistrations) {
            courseRepository.findById(reg.getCourseId()).ifPresent(courses::add);
        }

        return courses;
    }

}
