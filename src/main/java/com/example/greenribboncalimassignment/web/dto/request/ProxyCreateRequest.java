package com.example.greenribboncalimassignment.web.dto.request;

import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProxyCreateRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "보장 타입은 필수입니다.")
        GuaranteeType guaranteeType,

        @NotEmpty(message = "최소 1개 이상의 병원을 선택해야 합니다.")
        List<Long> hospitalIds
) {}