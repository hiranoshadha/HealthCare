package com.example.doctor_service;

import com.example.doctor_service.exception.ResourceNotFoundException;
import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.repository.DoctorScheduleRepository;
import com.example.doctor_service.service.serviceImpl.DoctorScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DoctorScheduleServiceImpl doctorScheduleService;

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

    // ---- createSchedule ----

    @Test
    void createSchedule_success() {
        when(restTemplate.getForObject(contains("/doctors/1"), any()))
                .thenReturn(new Object());
        when(restTemplate.getForObject(contains("/hospitals/1"), any()))
                .thenReturn(new Object());
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of());
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);

        DoctorSchedule result = doctorScheduleService.createSchedule(schedule);

        assertThat(result.getScheduleId()).isEqualTo(1L);
        assertThat(result.getDoctorId()).isEqualTo(1L);
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        verify(doctorScheduleRepository).save(any(DoctorSchedule.class));
    }

    @Test
    void createSchedule_invalidTimes_throwsException() {
        DoctorSchedule invalidSchedule = new DoctorSchedule();
        invalidSchedule.setDoctorId(1L);
        invalidSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        invalidSchedule.setStartTime(LocalTime.of(17, 0));
        invalidSchedule.setEndTime(LocalTime.of(9, 0)); // End before start
        invalidSchedule.setSlotDuration(30);

        when(restTemplate.getForObject(contains("/doctors/1"), any()))
                .thenReturn(new Object());

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(invalidSchedule))
                .isInstanceOf(Exception.class);

        verify(doctorScheduleRepository, never()).save(any());
    }

    // ---- getScheduleById ----

    @Test
    void getScheduleById_found() {
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        DoctorSchedule result = doctorScheduleService.getScheduleById(1L);

        assertThat(result.getScheduleId()).isEqualTo(1L);
        assertThat(result.getHospitalId()).isEqualTo(1L);
    }

    @Test
    void getScheduleById_notFound_throwsResourceNotFoundException() {
        when(doctorScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorScheduleService.getScheduleById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getAllSchedules ----

    @Test
    void getAllSchedules_returnsList() {
        DoctorSchedule schedule2 = new DoctorSchedule();
        schedule2.setScheduleId(2L);
        schedule2.setDoctorId(2L);
        schedule2.setHospitalId(1L);
        schedule2.setDayOfWeek(DayOfWeek.TUESDAY);
        schedule2.setStartTime(LocalTime.of(9, 0));
        schedule2.setEndTime(LocalTime.of(17, 0));
        schedule2.setSlotDuration(30);

        when(doctorScheduleRepository.findAll()).thenReturn(List.of(schedule, schedule2));

        List<DoctorSchedule> result = doctorScheduleService.getAllSchedules();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DoctorSchedule::getDoctorId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getAllSchedules_emptyList() {
        when(doctorScheduleRepository.findAll()).thenReturn(List.of());

        List<DoctorSchedule> result = doctorScheduleService.getAllSchedules();

        assertThat(result).isEmpty();
    }

    // ---- getSchedulesByDoctorId ----

    @Test
    void getSchedulesByDoctorId_returnsListForDoctor() {
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of(schedule));

        List<DoctorSchedule> result = doctorScheduleService.getSchedulesByDoctorId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctorId()).isEqualTo(1L);
    }

    // ---- getSchedulesByHospitalId ----

    @Test
    void getSchedulesByHospitalId_returnsListForHospital() {
        when(doctorScheduleRepository.findByHospitalId(1L)).thenReturn(List.of(schedule));

        List<DoctorSchedule> result = doctorScheduleService.getSchedulesByHospitalId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHospitalId()).isEqualTo(1L);
    }

    // ---- updateScheduleDay ----

    @Test
    void updateScheduleDay_success() {
        DoctorSchedule updated = new DoctorSchedule();
        updated.setScheduleId(1L);
        updated.setDoctorId(1L);
        updated.setHospitalId(1L);
        updated.setDayOfWeek(DayOfWeek.WEDNESDAY);
        updated.setStartTime(LocalTime.of(9, 0));
        updated.setEndTime(LocalTime.of(17, 0));
        updated.setSlotDuration(30);

        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(updated);

        DoctorSchedule result = doctorScheduleService.updateScheduleDay(1L, DayOfWeek.WEDNESDAY);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    void updateScheduleDay_notFound_throwsResourceNotFoundException() {
        when(doctorScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorScheduleService.updateScheduleDay(99L, DayOfWeek.WEDNESDAY))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- deleteSchedule ----

    @Test
    void deleteSchedule_success() {
        when(doctorScheduleRepository.existsById(1L)).thenReturn(true);

        doctorScheduleService.deleteSchedule(1L);

        verify(doctorScheduleRepository).deleteById(1L);
    }

    @Test
    void deleteSchedule_notFound_throwsResourceNotFoundException() {
        when(doctorScheduleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> doctorScheduleService.deleteSchedule(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorScheduleRepository, never()).deleteById(any());
    }

    // ---- calculateTotalSlots ----

    @Test
    void calculateTotalSlots_returnsCorrectCount() {
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        // 9:00 to 17:00 = 8 hours = 480 minutes / 30 minutes = 16 slots
        int result = doctorScheduleService.calculateTotalSlots(1L);

        assertThat(result).isEqualTo(16);
    }

    @Test
    void calculateTotalSlots_notFound_throwsResourceNotFoundException() {
        when(doctorScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorScheduleService.calculateTotalSlots(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getBookedSlots ----

    @Test
    void getBookedSlots_returnsCount() {
        when(restTemplate.getForObject(contains("/api/appointments/count/1"), eq(Long.class))).thenReturn(5L);

        long result = doctorScheduleService.getBookedSlots(1L);

        assertThat(result).isEqualTo(5L);
    }

    // ---- getRemainingSlots ----

    @Test
    void getRemainingSlots_returnsCalculatedCount() {
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(restTemplate.getForObject(contains("/api/appointments/count/1"), eq(Long.class))).thenReturn(5L);

        // 16 total - 5 booked = 11 remaining
        int result = doctorScheduleService.getRemainingSlots(1L);

        assertThat(result).isEqualTo(11);
    }

    @Test
    void getRemainingSlots_notFound_throwsResourceNotFoundException() {
        when(doctorScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorScheduleService.getRemainingSlots(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
