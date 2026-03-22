package com.example.appointment_service.repository;

import com.example.appointment_service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByScheduleId(Long scheduleId);
}