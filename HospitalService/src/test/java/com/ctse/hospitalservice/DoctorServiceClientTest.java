package com.ctse.hospitalservice;

import com.ctse.hospitalservice.client.DoctorServiceClient;
import com.ctse.hospitalservice.dto.DoctorScheduleDTO;
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
class DoctorServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private DoctorServiceClient doctorServiceClient;

    @BeforeEach
    void setUp() {
        doctorServiceClient = new DoctorServiceClient(restTemplate);
        ReflectionTestUtils.setField(doctorServiceClient, "doctorServiceUrl", "http://doctor-service");
    }

    @Test
    void getSchedulesByHospitalId_returnsSchedules() {
        DoctorScheduleDTO schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek("MONDAY");

        when(restTemplate.getForObject("http://doctor-service/api/schedules/hospital/1", DoctorScheduleDTO[].class))
                .thenReturn(new DoctorScheduleDTO[]{schedule});

        List<DoctorScheduleDTO> result = doctorServiceClient.getSchedulesByHospitalId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDayOfWeek()).isEqualTo("MONDAY");
    }

    @Test
    void getSchedulesByHospitalId_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject("http://doctor-service/api/schedules/hospital/1", DoctorScheduleDTO[].class))
                .thenReturn(null);

        List<DoctorScheduleDTO> result = doctorServiceClient.getSchedulesByHospitalId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getSchedulesByHospitalId_serviceUnavailable_returnsEmptyList() {
        when(restTemplate.getForObject("http://doctor-service/api/schedules/hospital/1", DoctorScheduleDTO[].class))
                .thenThrow(new RestClientException("Connection refused"));

        List<DoctorScheduleDTO> result = doctorServiceClient.getSchedulesByHospitalId(1L);

        assertThat(result).isEmpty();
    }
}
