package com.example.coursesystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RegistrationId.class)
public class Registration {
    @Id
    private Long studentId;

    @Id
    private Long courseId;

    private Long price;
    private LocalDateTime registeredDate;
}
