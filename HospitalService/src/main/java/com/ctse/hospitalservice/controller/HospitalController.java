package com.ctse.hospitalservice.controller;

import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;
import com.ctse.hospitalservice.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    public ResponseEntity<HospitalDTO> createHospital(@Valid @RequestBody HospitalDTO hospitalDTO) {
        return new ResponseEntity<>(hospitalService.createHospital(hospitalDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{hospitalId}")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(hospitalService.getHospitalById(hospitalId));
    }

    @GetMapping
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<HospitalDTO>> getHospitalsByCity(@PathVariable String city) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCity(city));
    }

    @PutMapping("/{hospitalId}")
    public ResponseEntity<HospitalDTO> updateHospital(@PathVariable Long hospitalId,
                                                      @Valid @RequestBody HospitalDTO hospitalDTO) {
        return ResponseEntity.ok(hospitalService.updateHospital(hospitalId, hospitalDTO));
    }

    @DeleteMapping("/{hospitalId}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long hospitalId) {
        hospitalService.deleteHospital(hospitalId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Inter-service communication endpoint.
     * Returns hospital details combined with all doctors fetched from User Service.
     * Used to demonstrate integration between Hospital Service and User Service.
     */
    @GetMapping("/{hospitalId}/doctors")
    public ResponseEntity<HospitalWithDoctorsDTO> getHospitalWithDoctors(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(hospitalService.getHospitalWithDoctors(hospitalId));
    }
}
