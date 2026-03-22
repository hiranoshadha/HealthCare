package com.example.appointment_service;

import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.serviceimpl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setScheduleId(1L);
        appointment.setPatientId(1L);
        appointment.setAppointmentDate(LocalDate.of(2026, 3, 25));
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setStatus("PENDING");
    }

    // ---- createAppointment ----

    @Test
    void createAppointment_success() {
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("scheduleId", 1);
        schedule.put("dayOfWeek", "MONDAY");
        schedule.put("startTime", "09:00");
        schedule.put("endTime", "17:00");
        schedule.put("slotDuration", 30);

        ResponseEntity<Map<String, Object>> mockResponse = new ResponseEntity<>(schedule, HttpStatus.OK);
        when(restTemplate.exchange(contains("/api/schedules/1"), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);
        when(appointmentRepository.findStartTimesByScheduleIdAndAppointmentDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment result = appointmentService.createAppointment(1L, 1L);

        assertThat(result.getAppointmentId()).isEqualTo(1L);
        assertThat(result.getScheduleId()).isEqualTo(1L);
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointment_scheduleNotFound_throwsException() {
        when(restTemplate.exchange(contains("/api/schedules/99"), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        assertThatThrownBy(() -> appointmentService.createAppointment(99L, 1L))
                .isInstanceOf(Exception.class);

        verify(appointmentRepository, never()).save(any());
    }

    // ---- getAppointmentById ----

    @Test
    void getAppointmentById_found() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentById(1L);

        assertThat(result.getAppointmentId()).isEqualTo(1L);
        assertThat(result.getPatientId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentById_notFound_throwsException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ---- getAllAppointments ----

    @Test
    void getAllAppointments_returnsList() {
        Appointment appointment2 = new Appointment();
        appointment2.setAppointmentId(2L);
        appointment2.setScheduleId(2L);
        appointment2.setPatientId(2L);
        appointment2.setAppointmentDate(LocalDate.of(2026, 3, 26));
        appointment2.setStartTime(LocalTime.of(10, 0));
        appointment2.setEndTime(LocalTime.of(10, 30));
        appointment2.setStatus("PENDING");

        when(appointmentRepository.findAll()).thenReturn(List.of(appointment, appointment2));

        List<Appointment> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Appointment::getPatientId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getAllAppointments_emptyList() {
        when(appointmentRepository.findAll()).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAllAppointments();

        assertThat(result).isEmpty();
    }

    // ---- getAppointmentsByScheduleId ----

    @Test
    void getAppointmentsByScheduleId_returnsListForSchedule() {
        when(appointmentRepository.findByScheduleId(1L)).thenReturn(List.of(appointment));

        List<Appointment> result = appointmentService.getAppointmentsByScheduleId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduleId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentsByScheduleId_empty() {
        when(appointmentRepository.findByScheduleId(1L)).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsByScheduleId(1L);

        assertThat(result).isEmpty();
    }

    // ---- getAppointmentsByPatientId ----

    @Test
    void getAppointmentsByPatientId_returnsListForPatient() {
        when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(appointment));

        List<Appointment> result = appointmentService.getAppointmentsByPatientId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentsByPatientId_empty() {
        when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsByPatientId(1L);

        assertThat(result).isEmpty();
    }

    // ---- deleteAppointment ----

    @Test
    void deleteAppointment_success() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    void deleteAppointment_notFound_throwsException() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(ResponseStatusException.class);

        verify(appointmentRepository, never()).deleteById(any());
    }

    // ---- countByScheduleId ----

    @ParameterizedTest
    @ValueSource(longs = {0L, 5L, 10L})
    void countByScheduleId_returnsCount(long count) {
        when(appointmentRepository.countByScheduleId(1L)).thenReturn(count);

        long result = appointmentService.countByScheduleId(1L);

        if (count == 0L) {
            assertThat(result).isZero();
        } else {
            assertThat(result).isEqualTo(count);
        }
    }
}
