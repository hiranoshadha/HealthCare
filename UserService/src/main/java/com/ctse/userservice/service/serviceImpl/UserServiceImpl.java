package com.ctse.userservice.service.serviceImpl;

import com.ctse.userservice.dto.*;
import com.ctse.userservice.model.Doctor;
import com.ctse.userservice.model.Patient;
import com.ctse.userservice.model.User;
import com.ctse.userservice.repository.UserRepository;
import com.ctse.userservice.repository.PatientRepository;
import com.ctse.userservice.repository.DoctorRepository;
import com.ctse.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private User createUser(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        return userRepository.save(user);
    }

    private PatientDTO mapToPatientDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setPatientId(patient.getPatientId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setAddress(patient.getAddress());
        dto.setEmail(patient.getEmail());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setEmergencyContactName(patient.getEmergencyContactName());
        dto.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        dto.setMedicalNotes(patient.getMedicalNotes());
        dto.setUserId(patient.getUserId());

        if (patient.getUser() != null) {
            dto.setUsername(patient.getUser().getUsername());
            dto.setRole(patient.getUser().getRole());
        }

        return dto;
    }

    private DoctorDTO mapToDoctorDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setEmail(doctor.getEmail());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setUserId(doctor.getUserId());

        if (doctor.getUser() != null) {
            dto.setUsername(doctor.getUser().getUsername());
            dto.setRole(doctor.getUser().getRole());
        }

        return dto;
    }

    @Override
    public Object login(String userName, String password) {
        User user = userRepository.findByUsernameAndPassword(
                userName, password
        ).orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if ("PATIENT".equals(user.getRole())) {
            Patient patient = patientRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            return mapToPatientDTO(patient);
        } else if ("DOCTOR".equals(user.getRole())) {
            Doctor doctor = doctorRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            return mapToDoctorDTO(doctor);
        } else {
            throw new RuntimeException("Invalid user role");
        }
    }

    // Patient operations
    @Override
    public PatientDTO createPatient(PatientDTO patientDTO, String password) {
        // Create user first
        User savedUser = createUser(patientDTO.getUsername(), password, "PATIENT");

        // Create patient
        Patient patient = new Patient();
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setBloodGroup(patientDTO.getBloodGroup());
        patient.setAddress(patientDTO.getAddress());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setEmergencyContactName(patientDTO.getEmergencyContactName());
        patient.setEmergencyContactPhone(patientDTO.getEmergencyContactPhone());
        patient.setMedicalNotes(patientDTO.getMedicalNotes());
        patient.setUserId(savedUser.getUserId());

        Patient savedPatient = patientRepository.save(patient);
        return mapToPatientDTO(savedPatient);
    }

    @Override
    public PatientDTO getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return mapToPatientDTO(patient);
    }

    @Override
    public PatientDTO getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user id: " + userId));
        return mapToPatientDTO(patient);
    }

    @Override
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToPatientDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDTO updatePatient(Long patientId, PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setBloodGroup(patientDTO.getBloodGroup());
        patient.setAddress(patientDTO.getAddress());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setEmergencyContactName(patientDTO.getEmergencyContactName());
        patient.setEmergencyContactPhone(patientDTO.getEmergencyContactPhone());
        patient.setMedicalNotes(patientDTO.getMedicalNotes());

        // Update username if provided
        if (patientDTO.getUsername() != null && patient.getUserId() != null) {
            User user = userRepository.findById(patient.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setUsername(patientDTO.getUsername());
            userRepository.save(user);
        }

        Patient updatedPatient = patientRepository.save(patient);
        return mapToPatientDTO(updatedPatient);
    }

    @Override
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Delete associated user if exists
        if (patient.getUserId() != null) {
            userRepository.deleteById(patient.getUserId());
        }

        patientRepository.deleteById(patientId);
    }

    // Doctor operations
    @Override
    public DoctorDTO createDoctor(DoctorDTO doctorDTO, String password) {
        // Create user first
        User savedUser = createUser(doctorDTO.getUsername(), password, "DOCTOR");

        // Create doctor
        Doctor doctor = new Doctor();
        doctor.setFirstName(doctorDTO.getFirstName());
        doctor.setLastName(doctorDTO.getLastName());
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setPhoneNumber(doctorDTO.getPhoneNumber());
        doctor.setUserId(savedUser.getUserId());

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToDoctorDTO(savedDoctor);
    }

    @Override
    public DoctorDTO getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return mapToDoctorDTO(doctor);
    }

    @Override
    public DoctorDTO getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for user id: " + userId));
        return mapToDoctorDTO(doctor);
    }

    @Override
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDoctorDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO updateDoctor(Long doctorId, DoctorDTO doctorDTO) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFirstName(doctorDTO.getFirstName());
        doctor.setLastName(doctorDTO.getLastName());
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setPhoneNumber(doctorDTO.getPhoneNumber());

        // Update username if provided
        if (doctorDTO.getUsername() != null && doctor.getUserId() != null) {
            User user = userRepository.findById(doctor.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setUsername(doctorDTO.getUsername());
            userRepository.save(user);
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return mapToDoctorDTO(updatedDoctor);
    }

    @Override
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Delete associated user if exists
        if (doctor.getUserId() != null) {
            userRepository.deleteById(doctor.getUserId());
        }

        doctorRepository.deleteById(doctorId);
    }
}