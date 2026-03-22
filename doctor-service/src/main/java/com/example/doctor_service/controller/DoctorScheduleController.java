package com.example.doctor_service.controller;

import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    public ResponseEntity<DoctorSchedule> createSchedule(@RequestBody DoctorSchedule schedule) {
        return new ResponseEntity<>(doctorScheduleService.createSchedule(schedule), HttpStatus.CREATED);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<DoctorSchedule> getScheduleById(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(doctorScheduleService.getScheduleById(scheduleId));
    }

    @GetMapping
    public ResponseEntity<List<DoctorSchedule>> getAllSchedules() {
        return ResponseEntity.ok(doctorScheduleService.getAllSchedules());
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getSchedulesByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorScheduleService.getSchedulesByDoctorId(doctorId));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<DoctorSchedule>> getSchedulesByHospitalId(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(doctorScheduleService.getSchedulesByHospitalId(hospitalId));
    }

    @PatchMapping("/{scheduleId}/day")
    public ResponseEntity<DoctorSchedule> updateScheduleDay(@PathVariable Long scheduleId,
                                                            @RequestParam DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(doctorScheduleService.updateScheduleDay(scheduleId, dayOfWeek));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        doctorScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{scheduleId}/slots/total")
    public ResponseEntity<Integer> getTotalSlots(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(doctorScheduleService.calculateTotalSlots(scheduleId));
    }

    @GetMapping("/{scheduleId}/slots/booked")
    public ResponseEntity<Long> getBookedSlots(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(doctorScheduleService.getBookedSlots(scheduleId));
    }

    @GetMapping("/{scheduleId}/slots/remaining")
    public ResponseEntity<Integer> getRemainingSlots(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(doctorScheduleService.getRemainingSlots(scheduleId));
    }
}