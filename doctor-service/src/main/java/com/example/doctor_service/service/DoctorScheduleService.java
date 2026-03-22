package com.example.doctor_service.service;

import com.example.doctor_service.model.DoctorSchedule;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleService {

    DoctorSchedule createSchedule(DoctorSchedule schedule);

    DoctorSchedule getScheduleById(Long scheduleId);

    List<DoctorSchedule> getSchedulesByDoctorId(Long doctorId);

    List<DoctorSchedule> getAllSchedules();

    List<DoctorSchedule> getSchedulesByHospitalId(Long hospitalId);

    DoctorSchedule updateScheduleDay(Long scheduleId, DayOfWeek dayOfWeek);

    void deleteSchedule(Long scheduleId);

    int calculateTotalSlots(Long scheduleId);

    long getBookedSlots(Long scheduleId);

    int getRemainingSlots(Long scheduleId);
}