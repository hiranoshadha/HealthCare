package com.ctse.hospitalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for doctor data received from User Service.
 * Mirrors the DoctorDTO structure in User Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfoDTO {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    private Long userId;
}
