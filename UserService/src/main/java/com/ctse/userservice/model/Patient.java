package com.ctse.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

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

    @Column(unique = true)
    private Long userId;

    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    private User user;
}