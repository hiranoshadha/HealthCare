package com.ctse.userservice.controller;

import com.ctse.userservice.dto.DoctorDTO;
import com.ctse.userservice.dto.PatientDTO;
import com.ctse.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody String userName, String password) {
        return ResponseEntity.ok(userService.login(userName, password));
    }

    // Patient endpoints
    @PostMapping("/patients")
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO,
                                                    @RequestParam String password) {
        return new ResponseEntity<>(userService.createPatient(patientDTO, password), HttpStatus.CREATED);
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long patientId) {
        return ResponseEntity.ok(userService.getPatientById(patientId));
    }

    @GetMapping("/patients/user/{userId}")
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getPatientByUserId(userId));
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(userService.getAllPatients());
    }

    @PutMapping("/patients/{patientId}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long patientId,
                                                    @RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(userService.updatePatient(patientId, patientDTO));
    }

    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        userService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }

    // Doctor endpoints
    @PostMapping("/doctors")
    public ResponseEntity<DoctorDTO> createDoctor(@RequestBody DoctorDTO doctorDTO,
                                                  @RequestParam String password) {
        return new ResponseEntity<>(userService.createDoctor(doctorDTO, password), HttpStatus.CREATED);
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long doctorId) {
        return ResponseEntity.ok(userService.getDoctorById(doctorId));
    }

    @GetMapping("/doctors/user/{userId}")
    public ResponseEntity<DoctorDTO> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getDoctorByUserId(userId));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        return ResponseEntity.ok(userService.getAllDoctors());
    }

    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable Long doctorId,
                                                  @RequestBody DoctorDTO doctorDTO) {
        return ResponseEntity.ok(userService.updateDoctor(doctorId, doctorDTO));
    }

    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long doctorId) {
        userService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }
}