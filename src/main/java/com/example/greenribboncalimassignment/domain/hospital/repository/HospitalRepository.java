package com.example.greenribboncalimassignment.domain.hospital.repository;

import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
