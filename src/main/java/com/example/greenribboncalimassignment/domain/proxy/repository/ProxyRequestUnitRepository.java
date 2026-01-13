package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestUnit;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProxyRequestUnitRepository extends JpaRepository<ProxyRequestUnit, Long> {

    // 검증 쿼리 : 특정 진료 기록 ID들이 특정 상태(COMPLETED, DISCLAIMER)의 신청서에 포함되어 있는지 확인
    boolean existsByUserTreatmentIdInAndProxyRequest_StatusIn(List<Long> treatmentIds, List<ProxyStatus> statuses);
}
