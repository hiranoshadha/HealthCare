package com.example.appointment_service.service;

import com.example.appointment_service.model.Appointment;

import java.util.List;

public interface AppointmentService {

    Appointment createAppointment(Long scheduleId, Long patientId);

    Appointment getAppointmentById(Long appointmentId);

    List<Appointment> getAllAppointments();

    List<Appointment> getAppointmentsByScheduleId(Long scheduleId);

    List<Appointment> getAppointmentsByPatientId(Long patientId);

    void deleteAppointment(Long appointmentId);

    long countByScheduleId(Long scheduleId);
}