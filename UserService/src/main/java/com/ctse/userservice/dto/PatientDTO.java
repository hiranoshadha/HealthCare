package com.ctse.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long patientId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String address;
    private String email;
    private String phoneNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalNotes;
    private Long userId;
    private String username;
    private String role;
}
