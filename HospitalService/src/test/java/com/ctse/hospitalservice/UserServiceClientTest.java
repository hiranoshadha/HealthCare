package com.ctse.hospitalservice;

import com.ctse.hospitalservice.client.UserServiceClient;
import com.ctse.hospitalservice.dto.DoctorInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        userServiceClient = new UserServiceClient(restTemplate);
        ReflectionTestUtils.setField(userServiceClient, "userServiceUrl", "http://user-service");
    }

    @Test
    void getAllDoctors_returnsDoctors() {
        DoctorInfoDTO doctor = new DoctorInfoDTO(1L, "Ishan", "Madusanka", "Dermatology",
                "LIC123", "ishan@h.com", "077", 2L);

        when(restTemplate.getForObject("http://user-service/api/users/doctors", DoctorInfoDTO[].class))
                .thenReturn(new DoctorInfoDTO[]{doctor});

        List<DoctorInfoDTO> result = userServiceClient.getAllDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ishan");
        assertThat(result.get(0).getSpecialization()).isEqualTo("Dermatology");
    }

    @Test
    void getAllDoctors_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject("http://user-service/api/users/doctors", DoctorInfoDTO[].class))
                .thenReturn(null);

        List<DoctorInfoDTO> result = userServiceClient.getAllDoctors();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllDoctors_serviceUnavailable_returnsEmptyList() {
        when(restTemplate.getForObject("http://user-service/api/users/doctors", DoctorInfoDTO[].class))
                .thenThrow(new RestClientException("Connection refused"));

        List<DoctorInfoDTO> result = userServiceClient.getAllDoctors();

        assertThat(result).isEmpty();
    }
}
