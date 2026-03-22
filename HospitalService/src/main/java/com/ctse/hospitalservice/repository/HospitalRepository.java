package com.ctse.hospitalservice.repository;

import com.ctse.hospitalservice.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByCity(String city);
    boolean existsByEmail(String email);
}
