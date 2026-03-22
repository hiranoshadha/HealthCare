package com.example.doctor_service.controller;

import com.example.doctor_service.dto.RemainingSlotsResponse;
import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/schedules")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService service;

    // Create new schedule
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DoctorSchedule schedule) {
        try {
            DoctorSchedule saved = service.createSchedule(schedule);
            return ResponseEntity.ok(saved);
        } catch (ResponseStatusException e) {
            String message = e.getReason() == null ? "Unable to create schedule" : e.getReason();
            return ResponseEntity.status(e.getStatusCode()).body(message);
        }
    }

    // Get schedule by ID
    @GetMapping("/{id}")
    public ResponseEntity<DoctorSchedule> get(@PathVariable Long id) {
        DoctorSchedule schedule = service.getSchedule(id);
        if (schedule != null) {
            return ResponseEntity.ok(schedule);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DoctorSchedule>> getAll() {
        return ResponseEntity.ok(service.getAllSchedules());
    }

    // Optional: Get all schedules for a doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getByDoctor(@PathVariable Long doctorId) {
        List<DoctorSchedule> schedules = service.getSchedulesByDoctorId(doctorId);
        return ResponseEntity.ok(schedules);
    }

    // Get all schedules for a hospital (used by Hospital Service)
    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<DoctorSchedule>> getByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(service.getSchedulesByHospitalId(hospitalId));
    }

    // Get remaining slots for a schedule
    @GetMapping("/remaining/{scheduleId}")
    public ResponseEntity<RemainingSlotsResponse> remaining(@PathVariable Long scheduleId) {
        int remaining = service.remainingSlots(scheduleId);
        return ResponseEntity.ok(new RemainingSlotsResponse(scheduleId, remaining));
    }
}