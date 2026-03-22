package com.ctse.hospitalservice.client;

import com.ctse.hospitalservice.dto.DoctorInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Client for communicating with the User Service.
 * Demonstrates inter-service communication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public List<DoctorInfoDTO> getAllDoctors() {
        try {
            String url = userServiceUrl + "/api/users/doctors";
            DoctorInfoDTO[] doctors = restTemplate.getForObject(url, DoctorInfoDTO[].class);
            return doctors != null ? Arrays.asList(doctors) : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Could not reach User Service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
