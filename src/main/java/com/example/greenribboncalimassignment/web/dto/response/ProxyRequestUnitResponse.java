package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record ProxyRequestUnitResponse(
        Long treatmentId,
        Long hospitalId,
        String hospitalName,
        LocalDate treatmentDate,
        Long missedAmount
) {

    @QueryProjection
    public ProxyRequestUnitResponse {
    }

    public static ProxyRequestUnitResponse from(UserTreatment treatment) {
        return new ProxyRequestUnitResponse(
                treatment.getId(),
                treatment.getHospital().getId(),
                treatment.getHospitalName(),
                treatment.getTreatmentDate(),
                treatment.getAmount()
        );
    }
}
