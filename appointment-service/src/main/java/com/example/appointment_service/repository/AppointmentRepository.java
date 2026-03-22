package com.example.appointment_service.repository;

import com.example.appointment_service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByScheduleId(Long scheduleId);

    boolean existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTime(
            Long scheduleId,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime
    );

    boolean existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTimeAndAppointmentIdNot(
            Long scheduleId,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            Long appointmentId
    );
}