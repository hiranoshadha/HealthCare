package com.ctse.userservice.dto;

import com.ctse.userservice.model.PatientBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PatientDTO extends PatientBase {
    private Long patientId;
    private Long userId;
    private String username;
    private String role;
}
