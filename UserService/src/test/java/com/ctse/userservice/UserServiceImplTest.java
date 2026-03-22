package com.ctse.userservice;

import com.ctse.userservice.dto.DoctorDTO;
import com.ctse.userservice.dto.PatientDTO;
import com.ctse.userservice.model.Doctor;
import com.ctse.userservice.model.Patient;
import com.ctse.userservice.model.User;
import com.ctse.userservice.repository.DoctorRepository;
import com.ctse.userservice.repository.PatientRepository;
import com.ctse.userservice.repository.UserRepository;
import com.ctse.userservice.security.JwtUtil;
import com.ctse.userservice.service.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private Patient patient;
    private PatientDTO patientDTO;
    private Doctor doctor;
    private DoctorDTO doctorDTO;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "johndoe", "hashedPassword", "PATIENT");

        patient = new Patient(
                1L, "John", "Doe", "1990-05-15", "Male", "O+",
                "123 Main St", "john@email.com", "0771234567",
                "Jane Doe", "0771234568", "No allergies", 1L, user
        );

        patientDTO = new PatientDTO(
                1L, "John", "Doe", "1990-05-15", "Male", "O+",
                "123 Main St", "john@email.com", "0771234567",
                "Jane Doe", "0771234568", "No allergies", 1L, "johndoe", "PATIENT"
        );

        doctor = new Doctor(
                1L, "Dr. Ishan", "Madusanka", "Dermatology",
                "LIC123456", "ishan@hospital.com", "0771234567", 2L, user
        );

        doctorDTO = new DoctorDTO(
                1L, "Dr. Ishan", "Madusanka", "Dermatology",
                "LIC123456", "ishan@hospital.com", "0771234567", 2L, "ishan_doc", "DOCTOR"
        );
    }

    // ---- createPatient ----

    @Test
    void createPatient_success() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientDTO result = userService.createPatient(patientDTO, "password123");

        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getUsername()).isEqualTo("johndoe");
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.createPatient(patientDTO, "password123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(patientRepository, never()).save(any());
    }

    // ---- getPatientById ----

    @Test
    void getPatientById_found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientDTO result = userService.getPatientById(1L);

        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getBloodGroup()).isEqualTo("O+");
    }

    @Test
    void getPatientById_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getPatientById(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- getPatientByUserId ----

    @Test
    void getPatientByUserId_found() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        PatientDTO result = userService.getPatientByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void getPatientByUserId_notFound_throwsException() {
        when(patientRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getPatientByUserId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- getAllPatients ----

    @Test
    void getAllPatients_returnsList() {
        Patient patient2 = new Patient(
                2L, "Jane", "Smith", "1992-03-20", "Female", "B+",
                "456 Oak Ave", "jane@email.com", "0772345678",
                "John Smith", "0772345679", "No conditions", 3L, user
        );
        when(patientRepository.findAll()).thenReturn(List.of(patient, patient2));

        List<PatientDTO> result = userService.getAllPatients();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PatientDTO::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void getAllPatients_emptyList() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<PatientDTO> result = userService.getAllPatients();

        assertThat(result).isEmpty();
    }

    // ---- updatePatient ----

    @Test
    void updatePatient_success() {
        Patient updated = new Patient(
                1L, "John", "Doe", "1990-05-15", "Male", "A+",
                "999 New St", "john@email.com", "0771234567",
                "Jane Doe", "0771234568", "Updated Notes", 1L, user
        );
        PatientDTO updateDTO = new PatientDTO(
                null, "John", "Doe", "1990-05-15", "Male", "A+",
                "999 New St", "john@email.com", "0771234567",
                "Jane Doe", "0771234568", "Updated Notes", 1L, "johndoe", "PATIENT"
        );

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        PatientDTO result = userService.updatePatient(1L, updateDTO);

        assertThat(result.getBloodGroup()).isEqualTo("A+");
        assertThat(result.getAddress()).isEqualTo("999 New St");
    }

    @Test
    void updatePatient_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePatient(99L, patientDTO))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- deletePatient ----

    @Test
    void deletePatient_success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        userService.deletePatient(1L);

        verify(patientRepository).deleteById(1L);
    }

    @Test
    void deletePatient_notFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deletePatient(99L))
                .isInstanceOf(RuntimeException.class);

        verify(patientRepository, never()).deleteById(any());
    }

    // ---- createDoctor ----

    @Test
    void createDoctor_success() {
        when(userRepository.existsByUsername("ishan_doc")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorDTO result = userService.createDoctor(doctorDTO, "password123");

        assertThat(result.getDoctorId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Dr. Ishan");
        assertThat(result.getSpecialization()).isEqualTo("Dermatology");
        verify(userRepository).save(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void createDoctor_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("ishan_doc")).thenReturn(true);

        assertThatThrownBy(() -> userService.createDoctor(doctorDTO, "password123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(doctorRepository, never()).save(any());
    }

    // ---- getDoctorById ----

    @Test
    void getDoctorById_found() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorDTO result = userService.getDoctorById(1L);

        assertThat(result.getDoctorId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Dr. Ishan");
        assertThat(result.getLicenseNumber()).isEqualTo("LIC123456");
    }

    @Test
    void getDoctorById_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDoctorById(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- getDoctorByUserId ----

    @Test
    void getDoctorByUserId_found() {
        when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));

        DoctorDTO result = userService.getDoctorByUserId(2L);

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getFirstName()).isEqualTo("Dr. Ishan");
    }

    @Test
    void getDoctorByUserId_notFound_throwsException() {
        when(doctorRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDoctorByUserId(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- getAllDoctors ----

    @Test
    void getAllDoctors_returnsList() {
        Doctor doctor2 = new Doctor(
                2L, "Dr. Amara", "Wijesinghe", "Cardiology",
                "LIC789012", "amara@hospital.com", "0779876543", 3L, user
        );
        when(doctorRepository.findAll()).thenReturn(List.of(doctor, doctor2));

        List<DoctorDTO> result = userService.getAllDoctors();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DoctorDTO::getFirstName)
                .containsExactlyInAnyOrder("Dr. Ishan", "Dr. Amara");
    }

    @Test
    void getAllDoctors_emptyList() {
        when(doctorRepository.findAll()).thenReturn(List.of());

        List<DoctorDTO> result = userService.getAllDoctors();

        assertThat(result).isEmpty();
    }

    // ---- updateDoctor ----

    @Test
    void updateDoctor_success() {
        Doctor updated = new Doctor(
                1L, "Dr. Ishan", "Madusanka", "Neurology",
                "LIC999999", "ishan.updated@hospital.com", "0779999999", 2L, user
        );
        DoctorDTO updateDTO = new DoctorDTO(
                null, "Dr. Ishan", "Madusanka", "Neurology",
                "LIC999999", "ishan.updated@hospital.com", "0779999999", 2L, "ishan_doc", "DOCTOR"
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(updated);

        DoctorDTO result = userService.updateDoctor(1L, updateDTO);

        assertThat(result.getSpecialization()).isEqualTo("Neurology");
        assertThat(result.getEmail()).isEqualTo("ishan.updated@hospital.com");
    }

    @Test
    void updateDoctor_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateDoctor(99L, doctorDTO))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- deleteDoctor ----

    @Test
    void deleteDoctor_success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        userService.deleteDoctor(1L);

        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void deleteDoctor_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteDoctor(99L))
                .isInstanceOf(RuntimeException.class);

        verify(doctorRepository, never()).deleteById(any());
    }
}
