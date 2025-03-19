package com.example.coursesystem.service;

import com.example.coursesystem.model.Course;
import com.example.coursesystem.model.Registration;
import com.example.coursesystem.model.Student;
import com.example.coursesystem.repository.CourseRepository;
import com.example.coursesystem.repository.RegistrationRepository;
import com.example.coursesystem.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class RegistrationServiceTest {
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Student student;
    private Course upcomingCourse;
    private Course ongoingCourse;
    private Course pastCourse;
    private Registration registration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        student = Student.builder()
                .id(1L)
                .email("leejimin@gmail.com")
                .firstName("Lee")
                .lastName("Jimin")
                .build();

        upcomingCourse = mock(Course.class);
        when(upcomingCourse.getId()).thenReturn(1L);
        when(upcomingCourse.getName()).thenReturn("Upcoming course");
        when(upcomingCourse.getStartTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(upcomingCourse.getEndTime()).thenReturn(LocalDateTime.now().plusDays(2));
        when(upcomingCourse.getPrice()).thenReturn(100000L);
        when(upcomingCourse.hasStarted()).thenReturn(false);

        ongoingCourse = mock(Course.class);
        when(ongoingCourse.getId()).thenReturn(2L);
        when(ongoingCourse.getName()).thenReturn("Ongoing course");
        when(ongoingCourse.getStartTime()).thenReturn(LocalDateTime.now().minusDays(1));
        when(ongoingCourse.getEndTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(ongoingCourse.getPrice()).thenReturn(100000L);
        when(ongoingCourse.hasStarted()).thenReturn(true);

        pastCourse = mock(Course.class);
        when(pastCourse.getId()).thenReturn(3L);
        when(pastCourse.getName()).thenReturn("Past course");
        when(pastCourse.getStartTime()).thenReturn(LocalDateTime.now().minusDays(3));
        when(pastCourse.getEndTime()).thenReturn(LocalDateTime.now().minusDays(2));
        when(pastCourse.getPrice()).thenReturn(100000L);
        when(pastCourse.hasStarted()).thenReturn(true);

        registration = Registration.builder()
                .studentId(student.getId())
                .courseId(upcomingCourse.getId())
                .price(upcomingCourse.getPrice())
                .registeredDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testRegisterCourse_Success() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.empty());
        when(registrationRepository.countOngoingCoursesByStudentId(anyLong(), any())).thenReturn(0);

        when(registrationRepository.findUpcomingRegistrationsByStudentId(anyLong(), any()))
                .thenReturn(List.of(registration));
        when(courseRepository.findById(registration.getCourseId())).thenReturn(Optional.of(upcomingCourse));
        List<Course> result = registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());

        verify(registrationRepository, times(1)).save(any(Registration.class));
        assertEquals(1, result.size());
        assertEquals(upcomingCourse.getId(), result.get(0).getId());
    }

    @Test
    void testRegisterCourse_WithDiscount() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.empty());
        when(registrationRepository.countOngoingCoursesByStudentId(anyLong(), any())).thenReturn(2);

        Registration registration1 = Registration.builder()
                .studentId(student.getId())
                .courseId(1L)
                .build();
        when(registrationRepository.findUpcomingRegistrationsByStudentId(anyLong(), any()))
                .thenReturn(List.of(registration1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(upcomingCourse));

        registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());

        verify(registrationRepository, times(1)).save(argThat(registration ->
                registration.getPrice().equals(upcomingCourse.getPrice() * 75 / 100)));
    }

    @Test
    void testRegisterCourse_AlreadyStarted() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(ongoingCourse.getId())).thenReturn(Optional.of(ongoingCourse));

        // Sử dụng spy hoặc mock hasStarted method để trả về true
        doReturn(true).when(ongoingCourse).hasStarted();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerCourse(student.getEmail(), ongoingCourse.getId());
        });
        assertEquals("Không thể đăng ký khóa học đã bắt đầu", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void testRegisterCourse_AlreadyRegistered() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.of(registration));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());
        });
        assertEquals("Đã đăng ký khóa học này rồi", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void testRegisterCourse_StudentNotFound() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());
        });
        assertEquals("Không tìm thấy học viên", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void testRegisterCourse_CourseNotFound() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());
        });
        assertEquals("Không tìm thấy khóa học", exception.getMessage());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void testUnregisterCourse_Success() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        doReturn(false).when(upcomingCourse).hasStarted();

        Registration registration = new Registration();
        registration.setStudentId(student.getId());
        registration.setCourseId(upcomingCourse.getId());

        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.of(registration));

        boolean result = registrationService.unregisterCourse(upcomingCourse.getId(), student.getEmail());

        assertTrue(result);
        verify(registrationRepository, times(1)).delete(registration);
    }

    @Test
    void testUnregisterCourse_AlreadyStarted() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(ongoingCourse.getId())).thenReturn(Optional.of(ongoingCourse));
        doReturn(true).when(ongoingCourse).hasStarted();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.unregisterCourse(ongoingCourse.getId(), student.getEmail());
        });

        assertEquals("Không thể hủy đăng ký khóa học đã bắt đầu", exception.getMessage());
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void testUnregisterCourse_NotRegistered() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        doReturn(false).when(upcomingCourse).hasStarted();

        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.unregisterCourse(upcomingCourse.getId(), student.getEmail());
        });

        assertEquals("Không tìm thấy đăng ký khóa học", exception.getMessage());
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void testUnregisterCourse_StudentNotFound() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.unregisterCourse(upcomingCourse.getId(), student.getEmail());
        });
        assertEquals("Không tìm thấy học viên", exception.getMessage());
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void testUnregisterCourse_CourseNotFound() {
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.unregisterCourse(upcomingCourse.getId(), student.getEmail());
        });
        assertEquals("Không tìm thấy khóa học", exception.getMessage());
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void testGetUpcomingRegisteredCourses() {
        Registration reg1 = Registration.builder()
                .studentId(student.getId())
                .courseId(upcomingCourse.getId())
                .build();
        Registration reg2 = Registration.builder()
                .studentId(student.getId())
                .courseId(2L)
                .build();

        when(registrationRepository.findUpcomingRegistrationsByStudentId(
                eq(student.getId()), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(reg1, reg2));

        when(courseRepository.findById(upcomingCourse.getId())).thenReturn(Optional.of(upcomingCourse));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(ongoingCourse));

        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse.getId()))
                .thenReturn(Optional.empty());
        when(registrationRepository.countOngoingCoursesByStudentId(anyLong(), any())).thenReturn(0);

        List<Course> result = registrationService.registerCourse(student.getEmail(), upcomingCourse.getId());

        assertEquals(2, result.size());
        assertTrue(result.contains(upcomingCourse));
        assertTrue(result.contains(ongoingCourse));
    }

}