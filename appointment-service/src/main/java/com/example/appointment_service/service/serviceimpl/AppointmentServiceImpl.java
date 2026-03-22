package com.example.appointment_service.service.serviceimpl;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final DateTimeFormatter HH_MM    = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;

    @Value("${doctor.service.url}")
    private String doctorServiceUrl;

    @Override
    public Appointment createAppointment(Long scheduleId, Long patientId) {

        Map<String, Object> schedule = fetchSchedule(scheduleId);

        DayOfWeek scheduledDay = parseDayOfWeek(schedule.get("dayOfWeek"));
        LocalDate appointmentDate = resolveNextAppointmentDate(scheduledDay);

        LocalTime scheduleStart   = parseTime(schedule.get("startTime"), "startTime");
        LocalTime scheduleEnd     = parseTime(schedule.get("endTime"),   "endTime");
        int       slotDuration    = parseSlotDuration(schedule.get("slotDuration"));

        LocalTime slotStart = resolveNextAvailableSlot(
                scheduleId, appointmentDate, scheduleStart, scheduleEnd, slotDuration
        );

        LocalTime slotEnd = slotStart.plusMinutes(slotDuration);

        if (slotEnd.isAfter(scheduleEnd)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No available slots remaining for schedule ID: " + scheduleId
                            + " on " + appointmentDate
            );
        }

        Appointment appointment = new Appointment();
        appointment.setScheduleId(scheduleId);
        appointment.setPatientId(patientId);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(slotStart);
        appointment.setEndTime(slotEnd);
        appointment.setStatus("PENDING");

        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found with ID: " + appointmentId
                ));
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<Appointment> getAppointmentsByScheduleId(Long scheduleId) {
        return appointmentRepository.findByScheduleId(scheduleId);
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public void deleteAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Appointment not found with ID: " + appointmentId
            );
        }
        appointmentRepository.deleteById(appointmentId);
    }

    @Override
    public long countByScheduleId(Long scheduleId) {
        return appointmentRepository.countByScheduleId(scheduleId);
    }

    private LocalDate resolveNextAppointmentDate(DayOfWeek scheduledDay) {
        LocalDate today = LocalDate.now();
        int daysUntil = (scheduledDay.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        return today.plusDays(daysUntil); // returns today if today matches
    }

    private LocalTime resolveNextAvailableSlot(Long scheduleId, LocalDate appointmentDate,
                                               LocalTime scheduleStart, LocalTime scheduleEnd,
                                               int slotDuration) {

        List<LocalTime> bookedStartTimes = appointmentRepository
                .findStartTimesByScheduleIdAndAppointmentDate(scheduleId, appointmentDate);

        LocalTime candidate = scheduleStart;

        while (!candidate.plusMinutes(slotDuration).isAfter(scheduleEnd)) {
            if (!bookedStartTimes.contains(candidate)) {
                return candidate;
            }
            candidate = candidate.plusMinutes(slotDuration);
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No available slots remaining for schedule ID: " + scheduleId
                        + " on " + appointmentDate
        );
    }

    private Map<String, Object> fetchSchedule(Long scheduleId) {
        String url = doctorServiceUrl + "/api/schedules/" + scheduleId;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + scheduleId
                );
            }

            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Schedule not found with ID: " + scheduleId
            );
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to reach doctor-service for schedule ID: " + scheduleId,
                    e
            );
        }
    }

    private DayOfWeek parseDayOfWeek(Object rawValue) {
        if (rawValue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule: missing dayOfWeek"
            );
        }
        try {
            return DayOfWeek.valueOf(rawValue.toString().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule: unrecognized dayOfWeek value: " + rawValue
            );
        }
    }

    private int parseSlotDuration(Object rawValue) {
        if (rawValue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule: missing slotDuration"
            );
        }
        try {
            int duration = ((Number) rawValue).intValue();
            if (duration <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid doctor schedule: slotDuration must be greater than 0"
                );
            }
            return duration;
        } catch (ClassCastException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule: slotDuration is not a valid number"
            );
        }
    }

    private LocalTime parseTime(Object rawValue, String fieldName) {
        if (rawValue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule: missing " + fieldName
            );
        }

        String value = rawValue.toString().trim();

        for (DateTimeFormatter formatter : List.of(HH_MM_SS, HH_MM)) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                // try next formatter
            }
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid doctor schedule time format for field: " + fieldName
            );
        }
    }
}