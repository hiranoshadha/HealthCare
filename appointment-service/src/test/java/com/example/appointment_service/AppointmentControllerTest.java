package com.example.appointment_service;

import com.example.appointment_service.controller.AppointmentController;
import com.example.appointment_service.exception.ResourceNotFoundException;
import com.example.appointment_service.model.Appointment;
import com.example.appointment_service.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

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

    @Test
    void createAppointment_returns201() {
        when(appointmentService.createAppointment(1L, 1L)).thenReturn(appointment);

        ResponseEntity<Appointment> response = appointmentController.createAppointment(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getAppointmentId()).isEqualTo(1L);
    }

    @Test
    void createAppointment_noAvailableSlots_throwsException() {
        when(appointmentService.createAppointment(1L, 1L))
                .thenThrow(new ResourceNotFoundException("No available slots for schedule ID: 1"));

        assertThatThrownBy(() -> appointmentController.createAppointment(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAppointmentById_returns200() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(appointment);

        ResponseEntity<Appointment> response = appointmentController.getAppointmentById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAppointmentId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentById_notFound_throwsException() {
        when(appointmentService.getAppointmentById(99L))
                .thenThrow(new ResourceNotFoundException("Appointment not found with id: 99"));

        assertThatThrownBy(() -> appointmentController.getAppointmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllAppointments_returns200() {
        Appointment appointment2 = new Appointment();
        appointment2.setAppointmentId(2L);
        appointment2.setScheduleId(2L);
        appointment2.setPatientId(2L);
        appointment2.setAppointmentDate(LocalDate.of(2026, 3, 26));
        appointment2.setStartTime(LocalTime.of(10, 0));
        appointment2.setEndTime(LocalTime.of(10, 30));
        appointment2.setStatus("PENDING");

        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment, appointment2));

        ResponseEntity<List<Appointment>> response = appointmentController.getAllAppointments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getAppointmentsByScheduleId_returns200() {
        when(appointmentService.getAppointmentsByScheduleId(1L)).thenReturn(List.of(appointment));

        ResponseEntity<List<Appointment>> response = appointmentController.getAppointmentsByScheduleId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAppointmentsByPatientId_returns200() {
        when(appointmentService.getAppointmentsByPatientId(1L)).thenReturn(List.of(appointment));

        ResponseEntity<List<Appointment>> response = appointmentController.getAppointmentsByPatientId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void deleteAppointment_returns204() {
        doNothing().when(appointmentService).deleteAppointment(1L);

        ResponseEntity<Void> response = appointmentController.deleteAppointment(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void countByScheduleId_returns200() {
        when(appointmentService.countByScheduleId(1L)).thenReturn(5L);

        ResponseEntity<Long> response = appointmentController.countByScheduleId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);
    }
}
