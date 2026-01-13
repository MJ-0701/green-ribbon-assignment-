package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record ProxyRequestUnitResponse(
        Long treatmentId,
        String hospitalName,
        LocalDate treatmentDate,
        Long missedAmount
) {

    // QueryDSL Q-Class 생성을 위한 생성자 프로젝션
    @QueryProjection
    public ProxyRequestUnitResponse {
    }

    // --- 정적 팩토리 메서드 (Entity -> DTO 변환용) ---
    public static ProxyRequestUnitResponse from(UserTreatment treatment) {
        return new ProxyRequestUnitResponse(
                treatment.getId(),
                treatment.getHospitalName(),
                treatment.getTreatmentDate(),
                treatment.getAmount()
        );
    }
}
