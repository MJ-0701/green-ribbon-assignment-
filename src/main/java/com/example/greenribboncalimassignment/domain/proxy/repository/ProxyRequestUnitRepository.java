package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestUnit;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProxyRequestUnitRepository extends JpaRepository<ProxyRequestUnit, Long> {

    // 검증 쿼리 : 종결(COMPLETED) 또는 면책(DISCLAIMER)된 병원은 다시 신청 불가능
    @Query("""
        SELECT COUNT(pru) > 0
        FROM ProxyRequestUnit pru
        JOIN pru.proxyRequest pr
        JOIN pru.userTreatment ut
        WHERE pr.user.id = :userId
          AND ut.hospital.id IN :hospitalIds
          AND pr.status IN :statuses
    """)
    boolean existsByHospitalIdInAndStatus(
            @Param("userId") Long userId,
            @Param("hospitalIds") List<Long> hospitalIds,
            @Param("statuses") List<ProxyStatus> statuses
    );

    default boolean existsProcessedHospital(Long userId, List<Long> hospitalIds) {
        return existsByHospitalIdInAndStatus(
                userId,
                hospitalIds,
                List.of(ProxyStatus.COMPLETED, ProxyStatus.DISCLAIMER)
        );
    }
}
