package com.example.appointment_service.controller;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestParam Long scheduleId,
                                                         @RequestParam Long patientId) {
        return new ResponseEntity<>(
                appointmentService.createAppointment(scheduleId, patientId),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByScheduleId(scheduleId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/{scheduleId}")
    public ResponseEntity<Long> countByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(appointmentService.countByScheduleId(scheduleId));
    }
}