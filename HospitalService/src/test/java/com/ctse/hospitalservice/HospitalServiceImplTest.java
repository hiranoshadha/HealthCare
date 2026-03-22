package com.ctse.hospitalservice;

import com.ctse.hospitalservice.client.DoctorServiceClient;
import com.ctse.hospitalservice.client.UserServiceClient;
import com.ctse.hospitalservice.dto.DoctorScheduleDTO;
import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;
import com.ctse.hospitalservice.exception.ResourceNotFoundException;
import com.ctse.hospitalservice.model.Hospital;
import com.ctse.hospitalservice.repository.HospitalRepository;
import com.ctse.hospitalservice.service.serviceimpl.HospitalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private DoctorServiceClient doctorServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private Hospital hospital;
    private HospitalDTO hospitalDTO;

    @BeforeEach
    void setUp() {
        hospital = new Hospital(1L, "City Hospital", "123 Main St", "Colombo", "0112345678", "city@hospital.com");
        hospitalDTO = new HospitalDTO(null, "City Hospital", "123 Main St", "Colombo", "0112345678", "city@hospital.com");
    }

    // ---- createHospital ----

    @Test
    void createHospital_success() {
        when(hospitalRepository.existsByEmail(hospitalDTO.getEmail())).thenReturn(false);
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        HospitalDTO result = hospitalService.createHospital(hospitalDTO);

        assertThat(result.getHospitalId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("City Hospital");
        assertThat(result.getEmail()).isEqualTo("city@hospital.com");
        verify(hospitalRepository).save(any(Hospital.class));
    }

    @Test
    void createHospital_duplicateEmail_throwsException() {
        when(hospitalRepository.existsByEmail(hospitalDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> hospitalService.createHospital(hospitalDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("already exists");

        verify(hospitalRepository, never()).save(any());
    }

    // ---- getHospitalById ----

    @Test
    void getHospitalById_found() {
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        HospitalDTO result = hospitalService.getHospitalById(1L);

        assertThat(result.getHospitalId()).isEqualTo(1L);
        assertThat(result.getCity()).isEqualTo("Colombo");
    }

    @Test
    void getHospitalById_notFound_throwsResourceNotFoundException() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getHospitalById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- getAllHospitals ----

    @Test
    void getAllHospitals_returnsList() {
        Hospital h2 = new Hospital(2L, "North Hospital", "45 Park Rd", "Kandy", "0812345678", "north@hospital.com");
        when(hospitalRepository.findAll()).thenReturn(List.of(hospital, h2));

        List<HospitalDTO> result = hospitalService.getAllHospitals();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(HospitalDTO::getName)
                .containsExactlyInAnyOrder("City Hospital", "North Hospital");
    }

    @Test
    void getAllHospitals_emptyList() {
        when(hospitalRepository.findAll()).thenReturn(List.of());

        List<HospitalDTO> result = hospitalService.getAllHospitals();

        assertThat(result).isEmpty();
    }

    // ---- getHospitalsByCity ----

    @Test
    void getHospitalsByCity_returnsMatchingHospitals() {
        when(hospitalRepository.findByCity("Colombo")).thenReturn(List.of(hospital));

        List<HospitalDTO> result = hospitalService.getHospitalsByCity("Colombo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Colombo");
    }

    // ---- updateHospital ----

    @Test
    void updateHospital_success() {
        HospitalDTO updateDTO = new HospitalDTO(null, "Updated Hospital", "99 New St", "Galle", "0912345678", "updated@hospital.com");
        Hospital updated = new Hospital(1L, "Updated Hospital", "99 New St", "Galle", "0912345678", "updated@hospital.com");

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(updated);

        HospitalDTO result = hospitalService.updateHospital(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Updated Hospital");
        assertThat(result.getCity()).isEqualTo("Galle");
    }

    @Test
    void updateHospital_notFound_throwsResourceNotFoundException() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.updateHospital(99L, hospitalDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- deleteHospital ----

    @Test
    void deleteHospital_success() {
        when(hospitalRepository.existsById(1L)).thenReturn(true);

        hospitalService.deleteHospital(1L);

        verify(hospitalRepository).deleteById(1L);
    }

    @Test
    void deleteHospital_notFound_throwsResourceNotFoundException() {
        when(hospitalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hospitalService.deleteHospital(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(hospitalRepository, never()).deleteById(any());
    }

    // ---- getHospitalWithDoctors ----

    @Test
    void getHospitalWithDoctors_returnsSchedulesFromDoctorService() {
        DoctorScheduleDTO schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek("MONDAY");
        schedule.setStartTime("09:00");
        schedule.setEndTime("17:00");
        schedule.setSlotDuration(30);

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(doctorServiceClient.getSchedulesByHospitalId(1L)).thenReturn(List.of(schedule));
        when(userServiceClient.getAllDoctors()).thenReturn(List.of());

        HospitalWithDoctorsDTO result = hospitalService.getHospitalWithDoctors(1L);

        assertThat(result.getHospitalId()).isEqualTo(1L);
        assertThat(result.getDoctors()).hasSize(1);
        assertThat(result.getDoctors().get(0).getDayOfWeek()).isEqualTo("MONDAY");
    }

    @Test
    void getHospitalWithDoctors_doctorServiceUnavailable_returnsEmptyDoctors() {
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(doctorServiceClient.getSchedulesByHospitalId(1L)).thenReturn(List.of());
        when(userServiceClient.getAllDoctors()).thenReturn(List.of());

        HospitalWithDoctorsDTO result = hospitalService.getHospitalWithDoctors(1L);

        assertThat(result.getDoctors()).isEmpty();
    }

    @Test
    void getHospitalWithDoctors_enrichesDoctorInfo() {
        DoctorScheduleDTO schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek("FRIDAY");

        com.ctse.hospitalservice.dto.DoctorInfoDTO doctorInfo =
                new com.ctse.hospitalservice.dto.DoctorInfoDTO(
                        1L, "Ishan", "Madusanka", "Dermatology", "LIC123", "ishan@h.com", "077", 2L);

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(doctorServiceClient.getSchedulesByHospitalId(1L)).thenReturn(List.of(schedule));
        when(userServiceClient.getAllDoctors()).thenReturn(List.of(doctorInfo));

        HospitalWithDoctorsDTO result = hospitalService.getHospitalWithDoctors(1L);

        assertThat(result.getDoctors()).hasSize(1);
        assertThat(result.getDoctors().get(0).getFirstName()).isEqualTo("Ishan");
        assertThat(result.getDoctors().get(0).getLastName()).isEqualTo("Madusanka");
        assertThat(result.getDoctors().get(0).getSpecialization()).isEqualTo("Dermatology");
    }

    @Test
    void getHospitalWithDoctors_hospitalNotFound_throwsResourceNotFoundException() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getHospitalWithDoctors(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHospitalsByCity_emptyResult_returnsEmptyList() {
        when(hospitalRepository.findByCity("Nowhere")).thenReturn(List.of());

        List<HospitalDTO> result = hospitalService.getHospitalsByCity("Nowhere");

        assertThat(result).isEmpty();
    }
}
