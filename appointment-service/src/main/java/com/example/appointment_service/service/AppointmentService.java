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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    
    // @Value("${DOCTOR_SERVICE_URL:http://doctor-service:8083}")
    
    @Value("${DOCTOR_SERVICE_URL:https://doctor-service-3h4t.onrender.com}")
      
    private String doctorServiceUrl;

    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public Appointment create(Appointment appointment) {
        Long scheduleId = appointment.getScheduleId();

        Map<String, Object> schedule = fetchSchedule(scheduleId);
        validateAppointmentWithinScheduleRange(
                appointment.getStartTime(),
                appointment.getEndTime(),
                schedule
        );

        validateTimeSlotNotTaken(
            scheduleId,
            appointment.getAppointmentDate(),
            appointment.getStartTime(),
            appointment.getEndTime()
        );

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

    public Appointment updateDateAndTime(Long appointmentId, Appointment updatedDateTime) {
        Optional<Appointment> existingOptional = repository.findById(appointmentId);
        if (existingOptional.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }

        Appointment existing = existingOptional.get();

        Map<String, Object> schedule = fetchSchedule(existing.getScheduleId());
        validateAppointmentWithinScheduleRange(
                updatedDateTime.getStartTime(),
                updatedDateTime.getEndTime(),
                schedule
        );

        boolean duplicateExists = repository.existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTimeAndAppointmentIdNot(
                existing.getScheduleId(),
                updatedDateTime.getAppointmentDate(),
                updatedDateTime.getStartTime(),
                updatedDateTime.getEndTime(),
                appointmentId
        );

        if (duplicateExists) {
            throw new IllegalStateException("Appointment time is already booked for this schedule");
        }

        existing.setAppointmentDate(updatedDateTime.getAppointmentDate());
        existing.setStartTime(updatedDateTime.getStartTime());
        existing.setEndTime(updatedDateTime.getEndTime());

        return repository.save(existing);
    }

    public void delete(Long appointmentId) {
        if (!repository.existsById(appointmentId)) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }

        repository.deleteById(appointmentId);
    }

    public long countBySchedule(Long scheduleId) {
        return repository.countByScheduleId(scheduleId);
    }

    public Appointment getById(Long appointmentId) {
        return repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));
    }

    public List<Appointment> getAll() {
        return repository.findAll();
    }

    private void validateTimeSlotNotTaken(Long scheduleId, java.time.LocalDate appointmentDate,
                                          java.time.LocalTime startTime, java.time.LocalTime endTime) {
        boolean duplicateExists = repository.existsByScheduleIdAndAppointmentDateAndStartTimeAndEndTime(
                scheduleId,
                appointmentDate,
                startTime,
                endTime
        );

        if (duplicateExists) {
            throw new IllegalStateException("Appointment time is already booked for this schedule");
        }
    }

    private Map<String, Object> fetchSchedule(Long scheduleId) {
        try {
            ResponseEntity<Map> scheduleResponse = restTemplate.getForEntity(
                    doctorServiceUrl + "/schedules/" + scheduleId,
                    Map.class
            );

            if (!scheduleResponse.getStatusCode().is2xxSuccessful() || scheduleResponse.getBody() == null) {
                throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
            }

            return scheduleResponse.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
        } catch (RestClientException e) {
            throw new IllegalStateException("Unable to validate schedule details for ID: " + scheduleId);
        }
    }

    private void validateAppointmentWithinScheduleRange(LocalTime appointmentStart,
                                                        LocalTime appointmentEnd,
                                                        Map<String, Object> schedule) {
        if (appointmentStart == null || appointmentEnd == null) {
            throw new IllegalStateException("Appointment startTime and endTime are required");
        }

        if (!appointmentEnd.isAfter(appointmentStart)) {
            throw new IllegalStateException("Appointment endTime must be after startTime");
        }

        LocalTime scheduleStart = parseTime(schedule.get("startTime"), "startTime");
        LocalTime scheduleEnd = parseTime(schedule.get("endTime"), "endTime");

        if (appointmentStart.isBefore(scheduleStart) || appointmentEnd.isAfter(scheduleEnd)) {
            throw new IllegalStateException("Appointment time must be within doctor schedule range: "
                    + scheduleStart + " - " + scheduleEnd);
        }
    }

    private LocalTime parseTime(Object rawValue, String fieldName) {
        if (rawValue == null) {
            throw new IllegalStateException("Invalid doctor schedule: missing " + fieldName);
        }

        String value = rawValue.toString().trim();

        try {
            return LocalTime.parse(value, HH_MM_SS);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(value, HH_MM);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Invalid doctor schedule time format for " + fieldName);
        }
    }
}
