package com.example.coursesystem.repository;

import com.example.coursesystem.model.Registration;
import com.example.coursesystem.model.RegistrationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, RegistrationId> {

    List<Registration> findByStudentId(Long studentId);

    @Query("SELECT r FROM Registration r JOIN Course c ON r.courseId = c.id " +
            "WHERE r.studentId = :studentId AND c.startTime > :now")
    List<Registration> findUpcomingRegistrationsByStudentId(Long studentId, LocalDateTime now);

    @Query("SELECT COUNT(r) FROM Registration r JOIN Course c ON r.courseId = c.id " +
            "WHERE r.studentId = :studentId AND c.startTime <= :now AND c.endTime > :now")
    int countOngoingCoursesByStudentId(Long studentId, LocalDateTime now);

    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
}