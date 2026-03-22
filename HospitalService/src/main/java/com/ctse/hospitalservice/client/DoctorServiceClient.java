package com.ctse.hospitalservice.client;

import com.ctse.hospitalservice.dto.DoctorScheduleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client for communicating with the Doctor Service.
 * Fetches schedules for a specific hospital.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorServiceClient {

    private final RestTemplate restTemplate;

    @Value("${doctor.service.url}")
    private String doctorServiceUrl;

    public List<DoctorScheduleDTO> getSchedulesByHospitalId(Long hospitalId) {
        try {
            String url = doctorServiceUrl + "/api/schedules/hospital/" + hospitalId;
            DoctorScheduleDTO[] schedules = restTemplate.getForObject(url, DoctorScheduleDTO[].class);
            return schedules != null ? Arrays.asList(schedules) : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Could not reach Doctor Service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
