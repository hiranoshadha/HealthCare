package com.ctse.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    private Long userId;
    private String username;
    private String role;
}
