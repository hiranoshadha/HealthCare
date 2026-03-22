package com.ctse.hospitalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for schedule data received from Doctor Service.
 * Mirrors the DoctorSchedule model in Doctor Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleDTO {
    private Long scheduleId;
    private Long doctorId;
    private Long hospitalId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int slotDuration;

    // Enriched from User Service
    private String firstName;
    private String lastName;
    private String specialization;
}
