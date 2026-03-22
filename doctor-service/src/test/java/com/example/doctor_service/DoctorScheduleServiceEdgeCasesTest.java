package com.example.doctor_service;

import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.repository.DoctorScheduleRepository;
import com.example.doctor_service.service.serviceimpl.DoctorScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceEdgeCasesTest {

    @Mock private DoctorScheduleRepository doctorScheduleRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private DoctorScheduleServiceImpl doctorScheduleService;

    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(doctorScheduleService, "userServiceUrl",     "http://user-service");
        ReflectionTestUtils.setField(doctorScheduleService, "hospitalServiceUrl", "http://hospital-service");
        ReflectionTestUtils.setField(doctorScheduleService, "appointmentServiceUrl", "http://appointment-service");

        schedule = new DoctorSchedule();
        schedule.setScheduleId(1L);
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setSlotDuration(30);
    }

    // ---- validateScheduleInput ----

    @Test
    void createSchedule_nullDoctorId_throwsBadRequest() {
        DoctorSchedule s = new DoctorSchedule();
        s.setDayOfWeek(DayOfWeek.MONDAY);
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(17, 0));
        s.setSlotDuration(30);

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(s))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createSchedule_nullDayOfWeek_throwsBadRequest() {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(1L);
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(17, 0));
        s.setSlotDuration(30);

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(s))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createSchedule_nullStartTime_throwsBadRequest() {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(1L);
        s.setDayOfWeek(DayOfWeek.MONDAY);
        s.setEndTime(LocalTime.of(17, 0));
        s.setSlotDuration(30);

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(s))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createSchedule_endBeforeStart_throwsBadRequest() {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(1L);
        s.setDayOfWeek(DayOfWeek.MONDAY);
        s.setStartTime(LocalTime.of(17, 0));
        s.setEndTime(LocalTime.of(9, 0));
        s.setSlotDuration(30);

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(s))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createSchedule_zeroSlotDuration_throwsBadRequest() {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(1L);
        s.setDayOfWeek(DayOfWeek.MONDAY);
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(17, 0));
        s.setSlotDuration(0);

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(s))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---- verifyDoctorExists ----

    @Test
    void createSchedule_doctorNotFound_throwsNotFound() {
        when(restTemplate.getForObject(contains("/doctors/1"), any()))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(schedule))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSchedule_doctorServiceDown_throwsBadGateway() {
        when(restTemplate.getForObject(contains("/doctors/1"), any()))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(schedule))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // ---- verifyHospitalExists ----

    @Test
    void createSchedule_hospitalNotFound_throwsNotFound() {
        when(restTemplate.getForObject(contains("/doctors/1"), any())).thenReturn(new Object());
        when(restTemplate.getForObject(contains("/hospitals/1"), any()))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(schedule))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSchedule_hospitalServiceDown_throwsBadGateway() {
        when(restTemplate.getForObject(contains("/doctors/1"), any())).thenReturn(new Object());
        when(restTemplate.getForObject(contains("/hospitals/1"), any()))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(schedule))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // ---- overlapping schedule ----

    @Test
    void createSchedule_overlap_throwsConflict() {
        DoctorSchedule existing = new DoctorSchedule();
        existing.setScheduleId(99L);
        existing.setDoctorId(1L);
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setStartTime(LocalTime.of(8, 0));
        existing.setEndTime(LocalTime.of(12, 0));

        when(restTemplate.getForObject(contains("/doctors/1"), any())).thenReturn(new Object());
        when(restTemplate.getForObject(contains("/hospitals/1"), any())).thenReturn(new Object());
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> doctorScheduleService.createSchedule(schedule))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createSchedule_noHospitalId_skipsHospitalVerification() {
        schedule.setHospitalId(null);
        when(restTemplate.getForObject(contains("/doctors/1"), any())).thenReturn(new Object());
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of());
        when(doctorScheduleRepository.save(any())).thenReturn(schedule);

        DoctorSchedule result = doctorScheduleService.createSchedule(schedule);

        assertThat(result.getScheduleId()).isEqualTo(1L);
        verify(restTemplate, never()).getForObject(contains("/hospitals/"), any());
    }

    // ---- getSchedulesByDoctorId with doctor not found ----

    @Test
    void getSchedulesByDoctorId_doctorNotFound_throwsNotFound() {
        when(restTemplate.getForObject(contains("/doctors/99"), any()))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> doctorScheduleService.getSchedulesByDoctorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---- getSchedulesByHospitalId with hospital not found ----

    @Test
    void getSchedulesByHospitalId_hospitalNotFound_throwsNotFound() {
        when(restTemplate.getForObject(contains("/hospitals/99"), any()))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> doctorScheduleService.getSchedulesByHospitalId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---- updateScheduleDay edge cases ----

    @Test
    void updateScheduleDay_nullDayOfWeek_throwsBadRequest() {
        assertThatThrownBy(() -> doctorScheduleService.updateScheduleDay(1L, null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateScheduleDay_overlap_throwsConflict() {
        DoctorSchedule conflicting = new DoctorSchedule();
        conflicting.setScheduleId(99L);
        conflicting.setDoctorId(1L);
        conflicting.setStartTime(LocalTime.of(8, 0));
        conflicting.setEndTime(LocalTime.of(12, 0));

        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.TUESDAY))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> doctorScheduleService.updateScheduleDay(1L, DayOfWeek.TUESDAY))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // ---- getBookedSlots edge cases ----

    @Test
    void getBookedSlots_nullResponse_returnsZero() {
        when(restTemplate.getForObject(contains("/api/appointments/count/1"), eq(Long.class)))
                .thenReturn(null);

        long result = doctorScheduleService.getBookedSlots(1L);

        assertThat(result).isZero();
    }

    @Test
    void getBookedSlots_serviceDown_throwsBadGateway() {
        when(restTemplate.getForObject(contains("/api/appointments/count/1"), eq(Long.class)))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> doctorScheduleService.getBookedSlots(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    // ---- calculateTotalSlots edge cases ----

    @Test
    void calculateTotalSlots_invalidSlotDuration_throwsUnprocessableEntity() {
        DoctorSchedule bad = new DoctorSchedule();
        bad.setScheduleId(1L);
        bad.setStartTime(LocalTime.of(9, 0));
        bad.setEndTime(LocalTime.of(17, 0));
        bad.setSlotDuration(0);
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(bad));

        assertThatThrownBy(() -> doctorScheduleService.calculateTotalSlots(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode().value())
                .isEqualTo(422);
    }

    @Test
    void calculateTotalSlots_endBeforeStart_throwsUnprocessableEntity() {
        DoctorSchedule bad = new DoctorSchedule();
        bad.setScheduleId(1L);
        bad.setStartTime(LocalTime.of(17, 0));
        bad.setEndTime(LocalTime.of(9, 0));
        bad.setSlotDuration(30);
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(bad));

        assertThatThrownBy(() -> doctorScheduleService.calculateTotalSlots(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode().value())
                .isEqualTo(422);
    }

    // ---- getRemainingSlots: booked >= total ----

    @Test
    void getRemainingSlots_bookedExceedsTotal_returnsZero() {
        when(doctorScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        // 16 total slots, 20 booked → max(16-20, 0) = 0
        when(restTemplate.getForObject(contains("/api/appointments/count/1"), eq(Long.class)))
                .thenReturn(20L);

        int result = doctorScheduleService.getRemainingSlots(1L);

        assertThat(result).isZero();
    }
}
