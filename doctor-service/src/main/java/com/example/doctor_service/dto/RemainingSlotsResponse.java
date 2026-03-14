package com.example.doctor_service.dto;

public class RemainingSlotsResponse {

    private Long scheduleId;
    private int remainingSlots;

    public RemainingSlotsResponse(Long scheduleId, int remainingSlots) {
        this.scheduleId = scheduleId;
        this.remainingSlots = remainingSlots;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }
}