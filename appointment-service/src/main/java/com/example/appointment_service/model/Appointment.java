package com.example.appointment_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    private Long scheduleId;
    private Long patientId;

    private LocalDate appointmentDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String status;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }
}