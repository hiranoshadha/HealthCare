package com.ctse.hospitalservice;

import com.ctse.hospitalservice.controller.HospitalController;
import com.ctse.hospitalservice.dto.DoctorScheduleDTO;
import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;
import com.ctse.hospitalservice.exception.GlobalExceptionHandler;
import com.ctse.hospitalservice.exception.ResourceNotFoundException;
import com.ctse.hospitalservice.service.HospitalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HospitalController.class)
@Import(GlobalExceptionHandler.class)
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HospitalService hospitalService;

    private HospitalDTO hospitalDTO;

    @BeforeEach
    void setUp() {
        hospitalDTO = new HospitalDTO(1L, "City Hospital", "123 Main St", "Colombo", "0112345678", "city@hospital.com");
    }

    // ---- POST /api/hospitals ----

    @Test
    void createHospital_returns201() throws Exception {
        when(hospitalService.createHospital(any(HospitalDTO.class))).thenReturn(hospitalDTO);

        mockMvc.perform(post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalId").value(1L))
                .andExpect(jsonPath("$.name").value("City Hospital"));
    }

    // ---- GET /api/hospitals/{id} ----

    @Test
    void getHospitalById_returns200() throws Exception {
        when(hospitalService.getHospitalById(1L)).thenReturn(hospitalDTO);

        mockMvc.perform(get("/api/hospitals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(1L))
                .andExpect(jsonPath("$.city").value("Colombo"));
    }

    @Test
    void getHospitalById_notFound_returns404() throws Exception {
        when(hospitalService.getHospitalById(99L))
                .thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        mockMvc.perform(get("/api/hospitals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Hospital not found with id: 99"));
    }

    // ---- GET /api/hospitals ----

    @Test
    void getAllHospitals_returns200() throws Exception {
        HospitalDTO h2 = new HospitalDTO(2L, "North Hospital", "45 Park Rd", "Kandy", "0812345678", "north@hospital.com");
        when(hospitalService.getAllHospitals()).thenReturn(List.of(hospitalDTO, h2));

        mockMvc.perform(get("/api/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("City Hospital"))
                .andExpect(jsonPath("$[1].name").value("North Hospital"));
    }

    @Test
    void getAllHospitals_empty_returns200() throws Exception {
        when(hospitalService.getAllHospitals()).thenReturn(List.of());

        mockMvc.perform(get("/api/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- GET /api/hospitals/city/{city} ----

    @Test
    void getHospitalsByCity_returns200() throws Exception {
        when(hospitalService.getHospitalsByCity("Colombo")).thenReturn(List.of(hospitalDTO));

        mockMvc.perform(get("/api/hospitals/city/Colombo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city").value("Colombo"));
    }

    // ---- PUT /api/hospitals/{id} ----

    @Test
    void updateHospital_returns200() throws Exception {
        HospitalDTO updated = new HospitalDTO(1L, "Updated Hospital", "99 New St", "Galle", "0912345678", "updated@hospital.com");
        when(hospitalService.updateHospital(eq(1L), any(HospitalDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/hospitals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hospital"))
                .andExpect(jsonPath("$.city").value("Galle"));
    }

    @Test
    void updateHospital_notFound_returns404() throws Exception {
        when(hospitalService.updateHospital(eq(99L), any(HospitalDTO.class)))
                .thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        mockMvc.perform(put("/api/hospitals/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDTO)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /api/hospitals/{id} ----

    @Test
    void deleteHospital_returns204() throws Exception {
        doNothing().when(hospitalService).deleteHospital(1L);

        mockMvc.perform(delete("/api/hospitals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteHospital_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Hospital not found with id: 99"))
                .when(hospitalService).deleteHospital(99L);

        mockMvc.perform(delete("/api/hospitals/99"))
                .andExpect(status().isNotFound());
    }

    // ---- GET /api/hospitals/{id}/doctors ----

    @Test
    void getHospitalWithDoctors_returns200() throws Exception {
        DoctorScheduleDTO schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(1L);
        schedule.setHospitalId(1L);
        schedule.setDayOfWeek("MONDAY");
        schedule.setStartTime("09:00");
        schedule.setEndTime("17:00");
        schedule.setSlotDuration(30);
        schedule.setFirstName("Ishan");
        schedule.setLastName("Madusanka");
        schedule.setSpecialization("Dermatology");

        HospitalWithDoctorsDTO result = new HospitalWithDoctorsDTO();
        result.setHospitalId(1L);
        result.setName("City Hospital");
        result.setDoctors(List.of(schedule));

        when(hospitalService.getHospitalWithDoctors(1L)).thenReturn(result);

        mockMvc.perform(get("/api/hospitals/1/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(1L))
                .andExpect(jsonPath("$.doctors.length()").value(1))
                .andExpect(jsonPath("$.doctors[0].firstName").value("Ishan"))
                .andExpect(jsonPath("$.doctors[0].specialization").value("Dermatology"));
    }

    @Test
    void getHospitalWithDoctors_notFound_returns404() throws Exception {
        when(hospitalService.getHospitalWithDoctors(99L))
                .thenThrow(new ResourceNotFoundException("Hospital not found with id: 99"));

        mockMvc.perform(get("/api/hospitals/99/doctors"))
                .andExpect(status().isNotFound());
    }
}
