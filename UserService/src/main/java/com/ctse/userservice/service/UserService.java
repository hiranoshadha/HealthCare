package com.ctse.userservice.service;

import com.ctse.userservice.dto.DoctorDTO;
import com.ctse.userservice.dto.PatientDTO;

import java.util.List;

public interface UserService {

    Object login(String userName, String password);

    // Patient operations
    PatientDTO createPatient(PatientDTO patientDTO, String password);
    PatientDTO getPatientById(Long patientId);
    PatientDTO getPatientByUserId(Long userId);
    List<PatientDTO> getAllPatients();
    PatientDTO updatePatient(Long patientId, PatientDTO patientDTO);
    void deletePatient(Long patientId);

    // Doctor operations
    DoctorDTO createDoctor(DoctorDTO doctorDTO, String password);
    DoctorDTO getDoctorById(Long doctorId);
    DoctorDTO getDoctorByUserId(Long userId);
    List<DoctorDTO> getAllDoctors();
    DoctorDTO updateDoctor(Long doctorId, DoctorDTO doctorDTO);
    void deleteDoctor(Long doctorId);
}
