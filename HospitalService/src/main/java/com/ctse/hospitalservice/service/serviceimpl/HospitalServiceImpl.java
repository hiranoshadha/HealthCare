package com.ctse.hospitalservice.service.serviceimpl;

import com.ctse.hospitalservice.client.DoctorServiceClient;
import com.ctse.hospitalservice.dto.HospitalDTO;
import com.ctse.hospitalservice.dto.HospitalWithDoctorsDTO;
import com.ctse.hospitalservice.exception.ResourceNotFoundException;
import com.ctse.hospitalservice.model.Hospital;
import com.ctse.hospitalservice.repository.HospitalRepository;
import com.ctse.hospitalservice.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalServiceImpl implements HospitalService {

    private static final String HOSPITAL_NOT_FOUND = "Hospital not found with id: ";

    private final HospitalRepository hospitalRepository;
    private final DoctorServiceClient doctorServiceClient;

    private HospitalDTO mapToDTO(Hospital hospital) {
        HospitalDTO dto = new HospitalDTO();
        dto.setHospitalId(hospital.getHospitalId());
        dto.setName(hospital.getName());
        dto.setAddress(hospital.getAddress());
        dto.setCity(hospital.getCity());
        dto.setContactNumber(hospital.getContactNumber());
        dto.setEmail(hospital.getEmail());
        return dto;
    }

    @Override
    public HospitalDTO createHospital(HospitalDTO hospitalDTO) {
        if (hospitalRepository.existsByEmail(hospitalDTO.getEmail())) {
            throw new ResourceNotFoundException("Hospital with this email already exists");
        }

        Hospital hospital = new Hospital();
        hospital.setName(hospitalDTO.getName());
        hospital.setAddress(hospitalDTO.getAddress());
        hospital.setCity(hospitalDTO.getCity());
        hospital.setContactNumber(hospitalDTO.getContactNumber());
        hospital.setEmail(hospitalDTO.getEmail());

        Hospital savedHospital = hospitalRepository.save(hospital);
        return mapToDTO(savedHospital);
    }

    @Override
    public HospitalDTO getHospitalById(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException(HOSPITAL_NOT_FOUND + hospitalId));
        return mapToDTO(hospital);
    }

    @Override
    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<HospitalDTO> getHospitalsByCity(String city) {
        return hospitalRepository.findByCity(city).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public HospitalDTO updateHospital(Long hospitalId, HospitalDTO hospitalDTO) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException(HOSPITAL_NOT_FOUND + hospitalId));

        hospital.setName(hospitalDTO.getName());
        hospital.setAddress(hospitalDTO.getAddress());
        hospital.setCity(hospitalDTO.getCity());
        hospital.setContactNumber(hospitalDTO.getContactNumber());
        hospital.setEmail(hospitalDTO.getEmail());

        Hospital updatedHospital = hospitalRepository.save(hospital);
        return mapToDTO(updatedHospital);
    }

    @Override
    public void deleteHospital(Long hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException(HOSPITAL_NOT_FOUND + hospitalId);
        }
        hospitalRepository.deleteById(hospitalId);
    }

    @Override
    public HospitalWithDoctorsDTO getHospitalWithDoctors(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException(HOSPITAL_NOT_FOUND + hospitalId));

        HospitalWithDoctorsDTO dto = new HospitalWithDoctorsDTO();
        dto.setHospitalId(hospital.getHospitalId());
        dto.setName(hospital.getName());
        dto.setAddress(hospital.getAddress());
        dto.setCity(hospital.getCity());
        dto.setContactNumber(hospital.getContactNumber());
        dto.setEmail(hospital.getEmail());
        dto.setDoctors(doctorServiceClient.getSchedulesByHospitalId(hospitalId));

        return dto;
    }
}
