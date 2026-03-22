package com.ctse.hospitalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Combined response DTO: hospital details + list of doctor schedules from Doctor Service.
 * Used for the inter-service communication endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalWithDoctorsDTO {
    private Long hospitalId;
    private String name;
    private String address;
    private String city;
    private String contactNumber;
    private String email;
    private List<DoctorScheduleDTO> doctors;
}
