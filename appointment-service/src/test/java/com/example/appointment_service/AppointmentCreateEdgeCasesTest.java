package com.example.appointment_service;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.serviceimpl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentCreateEdgeCasesTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentService, "doctorServiceUrl", "http://doctor-service");
    }

    private Map<String, Object> buildSchedule(String day, String start, String end, Object duration) {
        Map<String, Object> m = new HashMap<>();
        m.put("dayOfWeek", day);
        m.put("startTime", start);
        m.put("endTime", end);
        m.put("slotDuration", duration);
        return m;
    }

    private void mockFetchSchedule(long scheduleId, Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> resp = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(contains("/api/schedules/" + scheduleId),
                eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(resp);
    }

    // ---- fetchSchedule error paths ----

    @Test
    void createAppointment_scheduleServiceReturns404_throwsNotFound() {
        when(restTemplate.exchange(contains("/api/schedules/99"),
                eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> appointmentService.createAppointment(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createAppointment_scheduleServiceDown_throwsBadGateway() {
        when(restTemplate.exchange(contains("/api/schedules/1"),
                eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void createAppointment_scheduleBodyNull_throwsNotFound() {
        ResponseEntity<Map<String, Object>> resp = ResponseEntity.ok(null);
        when(restTemplate.exchange(contains("/api/schedules/1"),
                eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class)))
                .thenReturn(resp);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---- parseDayOfWeek paths ----

    @Test
    void createAppointment_missingDayOfWeek_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule(null, "09:00", "17:00", 30);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void createAppointment_invalidDayOfWeek_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule("NOTADAY", "09:00", "17:00", 30);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // ---- parseTime paths ----

    @Test
    void createAppointment_missingStartTime_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule("MONDAY", null, "17:00", 30);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void createAppointment_invalidTimeFormat_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "not-a-time", "17:00", 30);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void createAppointment_timeWithSeconds_parsedCorrectly() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "09:00:00", "09:30:00", 30);
        mockFetchSchedule(1L, schedule);
        when(appointmentRepository.findStartTimesByScheduleIdAndAppointmentDate(eq(1L), any()))
                .thenReturn(List.of());
        Appointment saved = new Appointment();
        saved.setAppointmentId(1L);
        saved.setStatus("PENDING");
        when(appointmentRepository.save(any())).thenReturn(saved);

        Appointment result = appointmentService.createAppointment(1L, 1L);

        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    // ---- parseSlotDuration paths ----

    @Test
    void createAppointment_missingSlotDuration_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "09:00", "17:00", null);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void createAppointment_zeroSlotDuration_throwsBadGateway() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "09:00", "17:00", 0);
        mockFetchSchedule(1L, schedule);

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // ---- resolveNextAvailableSlot / slot exhaustion ----

    @Test
    void createAppointment_allSlotsBooked_throwsConflict() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "09:00", "09:30", 30);
        mockFetchSchedule(1L, schedule);
        // Only slot 09:00–09:30 exists, and it's already booked
        when(appointmentRepository.findStartTimesByScheduleIdAndAppointmentDate(eq(1L), any()))
                .thenReturn(List.of(LocalTime.of(9, 0)));

        assertThatThrownBy(() -> appointmentService.createAppointment(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createAppointment_secondSlotAvailable_success() {
        Map<String, Object> schedule = buildSchedule("MONDAY", "09:00", "10:00", 30);
        mockFetchSchedule(1L, schedule);
        // First slot booked, second slot (09:30) is free
        when(appointmentRepository.findStartTimesByScheduleIdAndAppointmentDate(eq(1L), any()))
                .thenReturn(List.of(LocalTime.of(9, 0)));
        Appointment saved = new Appointment();
        saved.setStartTime(LocalTime.of(9, 30));
        saved.setStatus("PENDING");
        when(appointmentRepository.save(any())).thenReturn(saved);

        Appointment result = appointmentService.createAppointment(1L, 1L);

        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 30));
    }
}
