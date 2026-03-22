package com.example.appointment_service.repository;

import com.example.appointment_service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByScheduleId(Long scheduleId);

    List<Appointment> findByScheduleId(Long scheduleId);

    List<Appointment> findByPatientId(Long patientId);

    @Query("SELECT a.startTime FROM Appointment a WHERE a.scheduleId = :scheduleId AND a.appointmentDate = :date")
    List<LocalTime> findStartTimesByScheduleIdAndAppointmentDate(
            @Param("scheduleId") Long scheduleId,
            @Param("date") LocalDate date
    );

    boolean existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTime(
            Long scheduleId, LocalDate appointmentDate,
            LocalTime startTime, LocalTime endTime
    );

    boolean existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTimeAndAppointmentIdNot(
            Long scheduleId, LocalDate appointmentDate,
            LocalTime startTime, LocalTime endTime,
            Long appointmentId
    );
}