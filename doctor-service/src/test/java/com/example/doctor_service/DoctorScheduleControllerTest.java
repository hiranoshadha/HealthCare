package com.example.doctor_service;

import com.example.doctor_service.controller.DoctorScheduleController;
import com.example.doctor_service.exception.ResourceNotFoundException;
import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.service.DoctorScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleControllerTest {

    @Mock
    private DoctorScheduleService doctorScheduleService;

    @InjectMocks
    private DoctorScheduleController doctorScheduleController;

    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new DoctorSchedule();
        schedule.setScheduleId(1L);
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setSlotDuration(30);
    }

    @Test
    void createSchedule_returns201() {
        when(doctorScheduleService.createSchedule(any(DoctorSchedule.class))).thenReturn(schedule);

        ResponseEntity<DoctorSchedule> response = doctorScheduleController.createSchedule(schedule);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(schedule);
    }

    @Test
    void getScheduleById_returns200() {
        when(doctorScheduleService.getScheduleById(1L)).thenReturn(schedule);

        ResponseEntity<DoctorSchedule> response = doctorScheduleController.getScheduleById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScheduleId()).isEqualTo(1L);
    }

    @Test
    void getScheduleById_notFound_throwsException() {
        when(doctorScheduleService.getScheduleById(99L))
                .thenThrow(new ResourceNotFoundException("Schedule not found with id: 99"));

        assertThatThrownBy(() -> doctorScheduleController.getScheduleById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllSchedules_returns200() {
        DoctorSchedule schedule2 = new DoctorSchedule();
        schedule2.setScheduleId(2L);
        schedule2.setDoctorId(1L);
        schedule2.setHospitalId(1L);
        schedule2.setDayOfWeek(DayOfWeek.TUESDAY);
        schedule2.setStartTime(LocalTime.of(9, 0));
        schedule2.setEndTime(LocalTime.of(17, 0));
        schedule2.setSlotDuration(30);

        when(doctorScheduleService.getAllSchedules()).thenReturn(List.of(schedule, schedule2));

        ResponseEntity<List<DoctorSchedule>> response = doctorScheduleController.getAllSchedules();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getAllSchedules_empty_returns200() {
        when(doctorScheduleService.getAllSchedules()).thenReturn(List.of());

        ResponseEntity<List<DoctorSchedule>> response = doctorScheduleController.getAllSchedules();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getSchedulesByDoctorId_returns200() {
        when(doctorScheduleService.getSchedulesByDoctorId(1L)).thenReturn(List.of(schedule));

        ResponseEntity<List<DoctorSchedule>> response = doctorScheduleController.getSchedulesByDoctorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getSchedulesByHospitalId_returns200() {
        when(doctorScheduleService.getSchedulesByHospitalId(1L)).thenReturn(List.of(schedule));

        ResponseEntity<List<DoctorSchedule>> response = doctorScheduleController.getSchedulesByHospitalId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void updateScheduleDay_returns200() {
        DoctorSchedule updated = new DoctorSchedule();
        updated.setScheduleId(1L);
        updated.setDoctorId(1L);
        updated.setHospitalId(1L);
        updated.setDayOfWeek(DayOfWeek.WEDNESDAY);
        updated.setStartTime(LocalTime.of(9, 0));
        updated.setEndTime(LocalTime.of(17, 0));
        updated.setSlotDuration(30);

        when(doctorScheduleService.updateScheduleDay(eq(1L), eq(DayOfWeek.WEDNESDAY))).thenReturn(updated);

        ResponseEntity<DoctorSchedule> response = doctorScheduleController.updateScheduleDay(1L, DayOfWeek.WEDNESDAY);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    void deleteSchedule_returns204() {
        doNothing().when(doctorScheduleService).deleteSchedule(1L);

        ResponseEntity<Void> response = doctorScheduleController.deleteSchedule(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteSchedule_notFound_throwsException() {
        doThrow(new ResourceNotFoundException("Schedule not found with id: 99")).when(doctorScheduleService).deleteSchedule(99L);

        assertThatThrownBy(() -> doctorScheduleController.deleteSchedule(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTotalSlots_returns200() {
        when(doctorScheduleService.calculateTotalSlots(1L)).thenReturn(16);

        ResponseEntity<Integer> response = doctorScheduleController.getTotalSlots(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(16);
    }

    @Test
    void getBookedSlots_returns200() {
        when(doctorScheduleService.getBookedSlots(1L)).thenReturn(5L);

        ResponseEntity<Long> response = doctorScheduleController.getBookedSlots(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    void getRemainingSlots_returns200() {
        when(doctorScheduleService.getRemainingSlots(1L)).thenReturn(11);

        ResponseEntity<Integer> response = doctorScheduleController.getRemainingSlots(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(11);
    }
}
