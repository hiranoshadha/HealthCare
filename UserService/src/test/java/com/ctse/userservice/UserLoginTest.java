package com.ctse.userservice;

import com.ctse.userservice.dto.AdminDTO;
import com.ctse.userservice.dto.DoctorDTO;
import com.ctse.userservice.dto.LoginResponseDTO;
import com.ctse.userservice.dto.PatientDTO;
import com.ctse.userservice.exception.ResourceNotFoundException;
import com.ctse.userservice.model.Doctor;
import com.ctse.userservice.model.Patient;
import com.ctse.userservice.model.User;
import com.ctse.userservice.repository.DoctorRepository;
import com.ctse.userservice.repository.PatientRepository;
import com.ctse.userservice.repository.UserRepository;
import com.ctse.userservice.security.JwtUtil;
import com.ctse.userservice.service.serviceimpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User patientUser;
    private User doctorUser;
    private User adminUser;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patientUser = new User(1L, "johndoe", "hashedPassword", "PATIENT");
        doctorUser  = new User(2L, "ishan_doc", "hashedPassword", "DOCTOR");
        adminUser   = new User(3L, "admin", "hashedPassword", "ADMIN");

        patient = Patient.builder()
                .patientId(1L).firstName("John").lastName("Doe")
                .userId(1L).user(patientUser).build();

        doctor = new Doctor(1L, "Dr. Ishan", "Madusanka", "Dermatology",
                "LIC123", "ishan@h.com", "077", 2L, doctorUser);
    }

    @Test
    void login_patient_returnsPatientDTO() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(patientUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("johndoe", "PATIENT")).thenReturn("jwt-token");
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        Object result = userService.login("johndoe", "password123");

        assertThat(result).isInstanceOf(LoginResponseDTO.class);
        LoginResponseDTO response = (LoginResponseDTO) result;
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("PATIENT");
        assertThat(response.getUser()).isInstanceOf(PatientDTO.class);
    }

    @Test
    void login_doctor_returnsDoctorDTO() {
        when(userRepository.findByUsername("ishan_doc")).thenReturn(Optional.of(doctorUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("ishan_doc", "DOCTOR")).thenReturn("jwt-token");
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));

        Object result = userService.login("ishan_doc", "password123");

        assertThat(result).isInstanceOf(LoginResponseDTO.class);
        LoginResponseDTO response = (LoginResponseDTO) result;
        assertThat(response.getRole()).isEqualTo("DOCTOR");
        assertThat(response.getUser()).isInstanceOf(DoctorDTO.class);
    }

    @Test
    void login_admin_returnsAdminDTO() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("adminpass", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("jwt-token");

        Object result = userService.login("admin", "adminpass");

        assertThat(result).isInstanceOf(LoginResponseDTO.class);
        LoginResponseDTO response = (LoginResponseDTO) result;
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getUser()).isInstanceOf(AdminDTO.class);
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("unknown", "password123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(patientUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("johndoe", "wrongpassword"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void login_invalidRole_throwsException() {
        User unknownRoleUser = new User(4L, "other", "hashedPassword", "UNKNOWN");
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(unknownRoleUser));
        when(passwordEncoder.matches("pass", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("other", "UNKNOWN")).thenReturn("jwt-token");

        assertThatThrownBy(() -> userService.login("other", "pass"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void login_patient_profileNotFound_throwsException() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(patientUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("johndoe", "PATIENT")).thenReturn("jwt-token");
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("johndoe", "password123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient profile not found");
    }
}
