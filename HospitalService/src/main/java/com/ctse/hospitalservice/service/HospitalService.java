package com.ctse.hospitalservice.service;

import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;

import java.util.List;

public interface HospitalService {

    HospitalDTO createHospital(HospitalDTO hospitalDTO);
    HospitalDTO getHospitalById(Long hospitalId);
    List<HospitalDTO> getAllHospitals();
    List<HospitalDTO> getHospitalsByCity(String city);
    HospitalDTO updateHospital(Long hospitalId, HospitalDTO hospitalDTO);
    void deleteHospital(Long hospitalId);

    // Inter-service: fetches hospital info + doctors list from User Service
    HospitalWithDoctorsDTO getHospitalWithDoctors(Long hospitalId);
}
