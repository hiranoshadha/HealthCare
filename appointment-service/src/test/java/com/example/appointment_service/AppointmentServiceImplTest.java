package com.example.appointment_service;

import com.example.appointment_service.exception.ResourceNotFoundException;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.repository.AppointmentRepository;
import com.example.appointment_service.service.serviceImpl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        // Mock schedule retrieval from doctor service
        java.util.Map<String, Object> schedule = new java.util.HashMap<>();
        schedule.put("scheduleId", 1L);
        schedule.put("dayOfWeek", "MONDAY");
        schedule.put("startTime", "09:00");
        schedule.put("endTime", "17:00");
        schedule.put("slotDuration", 30);

        when(restTemplate.getForObject(contains("/schedules/1"), any()))
                .thenReturn(schedule);
        when(appointmentRepository.findStartTimesByScheduleIdAndAppointmentDate(1L, appointment.getAppointmentDate()))
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
        when(restTemplate.getForObject(contains("/schedules/99"), any()))
                .thenThrow(new ResourceNotFoundException("Schedule not found"));

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
    void getAppointmentById_notFound_throwsResourceNotFoundException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
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
    void deleteAppointment_notFound_throwsResourceNotFoundException() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).deleteById(any());
    }

    // ---- countByScheduleId ----

    @Test
    void countByScheduleId_returnsCorrectCount() {
        when(appointmentRepository.countByScheduleId(1L)).thenReturn(5L);

        long result = appointmentService.countByScheduleId(1L);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    void countByScheduleId_zeroCount() {
        when(appointmentRepository.countByScheduleId(1L)).thenReturn(0L);

        long result = appointmentService.countByScheduleId(1L);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void countByScheduleId_multipleAppointments() {
        when(appointmentRepository.countByScheduleId(1L)).thenReturn(10L);

        long result = appointmentService.countByScheduleId(1L);

        assertThat(result).isEqualTo(10L);
    }
}
