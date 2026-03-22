package com.example.doctor_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.doctor_service.model.converter.LocalTimeAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    private Long doctorId;
    private Long hospitalId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime endTime;

    private int slotDuration;
}