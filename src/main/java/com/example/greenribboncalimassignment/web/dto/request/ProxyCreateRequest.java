package com.example.greenribboncalimassignment.web.dto.request;

import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "청구 대행 신청 요청 DTO")
public record ProxyCreateRequest(
        @Schema(description = "신청하는 유저의 ID", example = "1")
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @Schema(description = "보장 타입 (NORMAL_PREPAID / NORMAL_POSTPAID / SPECIAL_PREPAID / SPECIAL_POSTPAID)", example = "NORMAL_POSTPAID")
        @NotNull(message = "보장 타입은 필수입니다.")
        GuaranteeType guaranteeType,

        @Schema(description = "신청할 병원 ID 목록 (최소 1개 이상)", example = "[1]")
        @NotEmpty(message = "최소 1개 이상의 병원을 선택해야 합니다.")
        List<Long> hospitalIds
) {}