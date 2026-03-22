package com.ctse.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public abstract class PatientBase {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
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
}
