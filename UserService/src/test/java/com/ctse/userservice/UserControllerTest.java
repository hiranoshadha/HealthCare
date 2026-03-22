package com.ctse.userservice;

import com.ctse.userservice.controller.UserController;
import com.ctse.userservice.dto.DoctorDTO;
import com.ctse.userservice.dto.LoginDTO;
import com.ctse.userservice.dto.LoginResponseDTO;
import com.ctse.userservice.dto.PatientDTO;
import com.ctse.userservice.exception.ResourceNotFoundException;
import com.ctse.userservice.service.UserService;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private PatientDTO patientDTO;
    private DoctorDTO doctorDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        patientDTO = new PatientDTO(
                1L, "John", "Doe", "1990-05-15", "Male", "O+",
                "123 Main St", "john@email.com", "0771234567",
                "Jane Doe", "0771234568", "No allergies",
                1L, "johndoe", "PATIENT"
        );

        doctorDTO = new DoctorDTO(
                1L, "Dr. Ishan", "Madusanka", "Dermatology",
                "LIC123456", "ishan@hospital.com", "0771234567",
                2L, "ishan_doc", "DOCTOR"
        );

        loginDTO = new LoginDTO("johndoe", "password123");
    }

    @Test
    void login_returns200() {
        LoginResponseDTO loginResponse = new LoginResponseDTO("jwt-token", patientDTO, "PATIENT");
        when(userService.login("johndoe", "password123")).thenReturn(loginResponse);

        ResponseEntity<Object> response = userController.login(loginDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(loginResponse);
    }

    @Test
    void login_invalidCredentials_throwsNotFound() {
        when(userService.login("johndoe", "wrongpassword"))
                .thenThrow(new ResourceNotFoundException("Invalid username or password"));

        LoginDTO loginDTO = new LoginDTO("johndoe", "wrongpassword");
        assertThatThrownBy(() -> userController.login(loginDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void createPatient_returns201() {
        when(userService.createPatient(any(PatientDTO.class), eq("password123"))).thenReturn(patientDTO);

        ResponseEntity<PatientDTO> response = userController.createPatient(patientDTO, "password123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(patientDTO);
    }

    @Test
    void getPatientById_returns200() {
        when(userService.getPatientById(1L)).thenReturn(patientDTO);

        ResponseEntity<PatientDTO> response = userController.getPatientById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPatientId()).isEqualTo(1L);
    }

    @Test
    void getPatientById_notFound_throwsNotFound() {
        when(userService.getPatientById(99L)).thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        assertThatThrownBy(() -> userController.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPatientByUserId_returns200() {
        when(userService.getPatientByUserId(1L)).thenReturn(patientDTO);

        ResponseEntity<PatientDTO> response = userController.getPatientByUserId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserId()).isEqualTo(1L);
    }

    @Test
    void getAllPatients_returns200() {
        PatientDTO patient2 = new PatientDTO(
                2L, "Jane", "Smith", "1992-03-20", "Female", "B+",
                "456 Oak Ave", "jane@email.com", "0772345678",
                "John Smith", "0772345679", "No conditions",
                3L, "janesmith", "PATIENT"
        );
        when(userService.getAllPatients()).thenReturn(List.of(patientDTO, patient2));

        ResponseEntity<List<PatientDTO>> response = userController.getAllPatients();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void updatePatient_returns200() {
        PatientDTO updated = new PatientDTO(
                1L, "John", "Doe", "1990-05-15", "Male", "A+",
                "999 New St", "newemail@email.com", "0779999999",
                "Jane Doe", "0771234568", "Updated notes",
                1L, "johndoe", "PATIENT"
        );
        when(userService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(updated);

        ResponseEntity<PatientDTO> response = userController.updatePatient(1L, updated);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBloodGroup()).isEqualTo("A+");
    }

    @Test
    void deletePatient_returns204() {
        doNothing().when(userService).deletePatient(1L);

        ResponseEntity<Void> response = userController.deletePatient(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createDoctor_returns201() {
        when(userService.createDoctor(any(DoctorDTO.class), eq("password123"))).thenReturn(doctorDTO);

        ResponseEntity<DoctorDTO> response = userController.createDoctor(doctorDTO, "password123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(doctorDTO);
    }

    @Test
    void getDoctorById_returns200() {
        when(userService.getDoctorById(1L)).thenReturn(doctorDTO);

        ResponseEntity<DoctorDTO> response = userController.getDoctorById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDoctorId()).isEqualTo(1L);
    }

    @Test
    void getDoctorByUserId_returns200() {
        when(userService.getDoctorByUserId(2L)).thenReturn(doctorDTO);

        ResponseEntity<DoctorDTO> response = userController.getDoctorByUserId(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUserId()).isEqualTo(2L);
    }

    @Test
    void getAllDoctors_returns200() {
        DoctorDTO doctor2 = new DoctorDTO(
                2L, "Dr. Amara", "Wijesinghe", "Cardiology",
                "LIC789012", "amara@hospital.com", "0779876543",
                3L, "amara_doc", "DOCTOR"
        );
        when(userService.getAllDoctors()).thenReturn(List.of(doctorDTO, doctor2));

        ResponseEntity<List<DoctorDTO>> response = userController.getAllDoctors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void updateDoctor_returns200() {
        DoctorDTO updated = new DoctorDTO(
                1L, "Dr. Ishan", "Madusanka", "Neurology",
                "LIC999999", "ishan.updated@hospital.com", "0779999999",
                2L, "ishan_doc", "DOCTOR"
        );
        when(userService.updateDoctor(eq(1L), any(DoctorDTO.class))).thenReturn(updated);

        ResponseEntity<DoctorDTO> response = userController.updateDoctor(1L, updated);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSpecialization()).isEqualTo("Neurology");
    }

    @Test
    void deleteDoctor_returns204() {
        doNothing().when(userService).deleteDoctor(1L);

        ResponseEntity<Void> response = userController.deleteDoctor(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
