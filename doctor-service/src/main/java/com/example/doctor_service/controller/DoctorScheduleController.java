package com.example.doctor_service.controller;

import com.example.doctor_service.dto.RemainingSlotsResponse;
import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService service;

    // Create new schedule
    @PostMapping
    public ResponseEntity<DoctorSchedule> create(@RequestBody DoctorSchedule schedule) {
        DoctorSchedule saved = service.createSchedule(schedule);
        return ResponseEntity.ok(saved);
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

    // Optional: Get all schedules for a doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getByDoctor(@PathVariable Long doctorId) {
        List<DoctorSchedule> schedules = service.getSchedulesByDoctorId(doctorId);
        return ResponseEntity.ok(schedules);
    }

    // Get remaining slots for a schedule
    @GetMapping("/remaining/{scheduleId}")
    public ResponseEntity<RemainingSlotsResponse> remaining(@PathVariable Long scheduleId) {
        int remaining = service.remainingSlots(scheduleId);
        return ResponseEntity.ok(new RemainingSlotsResponse(scheduleId, remaining));
    }
}