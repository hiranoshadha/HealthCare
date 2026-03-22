package com.ctse.hospitalservice;

import com.ctse.hospitalservice.controller.HospitalController;
import com.ctse.hospitalservice.dto.DoctorScheduleDTO;
import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;
import com.ctse.hospitalservice.exception.ResourceNotFoundException;
import com.ctse.hospitalservice.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalControllerTest {

    @Mock
    private HospitalService hospitalService;

    @InjectMocks
    private HospitalController hospitalController;

    private HospitalDTO hospitalDTO;

    @BeforeEach
    void setUp() {
        hospitalDTO = new HospitalDTO(1L, "City Hospital", "123 Main St", "Colombo", "0112345678", "city@hospital.com");
    }

    // ---- createHospital ----

    @Test
    void createHospital_returns201() {
        when(hospitalService.createHospital(any(HospitalDTO.class))).thenReturn(hospitalDTO);

        ResponseEntity<HospitalDTO> response = hospitalController.createHospital(hospitalDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("City Hospital");
    }

    // ---- getHospitalById ----

    @Test
    void getHospitalById_returns200() {
        when(hospitalService.getHospitalById(1L)).thenReturn(hospitalDTO);

        ResponseEntity<HospitalDTO> response = hospitalController.getHospitalById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getHospitalId()).isEqualTo(1L);
    }

    @Test
    void getHospitalById_notFound_throwsException() {
        when(hospitalService.getHospitalById(99L)).thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        assertThatThrownBy(() -> hospitalController.getHospitalById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- getAllHospitals ----

    @Test
    void getAllHospitals_returnsList() {
        HospitalDTO h2 = new HospitalDTO(2L, "North Hospital", "45 Park Rd", "Kandy", "0812345678", "north@hospital.com");
        when(hospitalService.getAllHospitals()).thenReturn(List.of(hospitalDTO, h2));

        ResponseEntity<List<HospitalDTO>> response = hospitalController.getAllHospitals();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getAllHospitals_empty() {
        when(hospitalService.getAllHospitals()).thenReturn(List.of());

        ResponseEntity<List<HospitalDTO>> response = hospitalController.getAllHospitals();

        assertThat(response.getBody()).isEmpty();
    }

    // ---- getHospitalsByCity ----

    @Test
    void getHospitalsByCity_returnsList() {
        when(hospitalService.getHospitalsByCity("Colombo")).thenReturn(List.of(hospitalDTO));

        ResponseEntity<List<HospitalDTO>> response = hospitalController.getHospitalsByCity("Colombo");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCity()).isEqualTo("Colombo");
    }

    // ---- updateHospital ----

    @Test
    void updateHospital_returns200() {
        HospitalDTO updated = new HospitalDTO(1L, "Updated Hospital", "99 New St", "Galle", "0912345678", "updated@hospital.com");
        when(hospitalService.updateHospital(eq(1L), any(HospitalDTO.class))).thenReturn(updated);

        ResponseEntity<HospitalDTO> response = hospitalController.updateHospital(1L, updated);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Hospital");
    }

    @Test
    void updateHospital_notFound_throwsException() {
        when(hospitalService.updateHospital(eq(99L), any(HospitalDTO.class)))
                .thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        assertThatThrownBy(() -> hospitalController.updateHospital(99L, hospitalDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- deleteHospital ----

    @Test
    void deleteHospital_returns204() {
        doNothing().when(hospitalService).deleteHospital(1L);

        ResponseEntity<Void> response = hospitalController.deleteHospital(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(hospitalService).deleteHospital(1L);
    }

    @Test
    void deleteHospital_notFound_throwsException() {
        doThrow(new ResourceNotFoundException("Hospital not found with id: 99"))
                .when(hospitalService).deleteHospital(99L);

        assertThatThrownBy(() -> hospitalController.deleteHospital(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getHospitalWithDoctors ----

    @Test
    void getHospitalWithDoctors_returns200() {
        DoctorScheduleDTO schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(1L);
        schedule.setFirstName("Ishan");
        schedule.setLastName("Madusanka");
        schedule.setSpecialization("Dermatology");

        HospitalWithDoctorsDTO result = new HospitalWithDoctorsDTO();
        result.setHospitalId(1L);
        result.setName("City Hospital");
        result.setDoctors(List.of(schedule));

        when(hospitalService.getHospitalWithDoctors(1L)).thenReturn(result);

        ResponseEntity<HospitalWithDoctorsDTO> response = hospitalController.getHospitalWithDoctors(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDoctors()).hasSize(1);
        assertThat(response.getBody().getDoctors().get(0).getFirstName()).isEqualTo("Ishan");
    }

    @Test
    void getHospitalWithDoctors_notFound_throwsException() {
        when(hospitalService.getHospitalWithDoctors(99L))
                .thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        assertThatThrownBy(() -> hospitalController.getHospitalWithDoctors(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}