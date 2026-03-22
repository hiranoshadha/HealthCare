package com.example.doctor_service.repository;

import com.example.doctor_service.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
	List<DoctorSchedule> findByDoctorId(Long doctorId);

	List<DoctorSchedule> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

	List<DoctorSchedule> findByHospitalId(Long hospitalId);
}