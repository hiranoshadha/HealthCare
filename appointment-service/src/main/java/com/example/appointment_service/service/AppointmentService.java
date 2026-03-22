package com.example.appointment_service.service;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AppointmentService {

    @Value("${doctor.service.url:http://localhost:8081}")
    private String doctorServiceUrl;

    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public Appointment create(Appointment appointment) {
        Long scheduleId = appointment.getScheduleId();

        // 1. Check schedule exists in doctor-service
        try {
            ResponseEntity<Object> scheduleResponse = restTemplate.getForEntity(
                    doctorServiceUrl + "/schedules/" + scheduleId, Object.class);
            if (!scheduleResponse.getStatusCode().is2xxSuccessful() || scheduleResponse.getBody() == null) {
                throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
        }

        // 2. Check remaining slots
        ResponseEntity<Map> remainingResponse;
        try {
            remainingResponse = restTemplate.getForEntity(
                    doctorServiceUrl + "/schedules/remaining/" + scheduleId,
                    Map.class
            );
        } catch (RestClientException e) {
            throw new IllegalStateException("Unable to validate remaining slots for schedule ID: " + scheduleId);
        }

        Map<String, Object> body = remainingResponse.getBody();
        Number remainingValue = body == null ? null : (Number) body.get("remainingSlots");
        int remaining = remainingValue == null ? 0 : remainingValue.intValue();

        if (remaining <= 0) {
            throw new IllegalStateException("No remaining slots available for schedule ID: " + scheduleId);
        }

        return repository.save(appointment);
    }

    public long countBySchedule(Long scheduleId) {
        return repository.countByScheduleId(scheduleId);
    }
}