package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTreatmentRepository extends JpaRepository<UserTreatment, Long>, UserTreatmentRepositoryCustom {

    // [3.1용] JPA Method (Entity 반환, 특정 ID 목록 조회)
    List<UserTreatment> findAllByUserIdAndHospital_IdIn(Long userId, List<Long> hospitalIds);
}
