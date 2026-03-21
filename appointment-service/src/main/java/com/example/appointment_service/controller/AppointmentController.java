package com.example.appointment_service.controller;

import com.example.appointment_service.dto.AppointmentDateTimeUpdateRequest;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private static final DateTimeFormatter H_MM = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Appointment appointment) {
        try {
            Appointment saved = service.create(appointment);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/count/{scheduleId}")
    public long count(@PathVariable Long scheduleId) {
        return service.countBySchedule(scheduleId);
    }

    @GetMapping
    public List<Appointment> getAll() {
        return service.getAll();
    }

   
    

    @PutMapping("/{appointmentId}/datetime")
    public ResponseEntity<?> updateDateAndTime(@PathVariable Long appointmentId,
                                               @RequestBody AppointmentDateTimeUpdateRequest request) {
        try {
            Appointment appointment = new Appointment();
            appointment.setAppointmentDate(request.getAppointmentDate());
            appointment.setStartTime(parseTime(request.getStartTime()));
            appointment.setEndTime(parseTime(request.getEndTime()));

            Appointment updated = service.updateDateAndTime(appointmentId, appointment);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> delete(@PathVariable Long appointmentId) {
        try {
            service.delete(appointmentId);
            return ResponseEntity.ok("Appointment deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Time is required. Use format HH:mm or HH:mm:ss");
        }

        String trimmed = value.trim();

        try {
            return LocalTime.parse(trimmed, H_MM);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(trimmed, HH_MM);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(trimmed, HH_MM_SS);
        } catch (DateTimeParseException ignored) {
        }

        throw new IllegalStateException("Invalid time format. Use HH:mm or HH:mm:ss");
    }
}