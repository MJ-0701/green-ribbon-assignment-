package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "진료 기록 단위 응답 DTO")
public record ProxyRequestUnitResponse(
        @Schema(description = "진료 기록 ID", example = "1")
        Long treatmentId,

        @Schema(description = "병원 ID", example = "1")
        Long hospitalId,

        @Schema(description = "병원 이름", example = "세브란스병원")
        String hospitalName,

        @Schema(description = "진료 일자", example = "2023-10-25")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate treatmentDate,

        @Schema(description = "진료비 (누락 보험금)", example = "100000")
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