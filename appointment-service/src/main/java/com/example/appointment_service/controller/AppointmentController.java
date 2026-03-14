package com.example.appointment_service.controller;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

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
}