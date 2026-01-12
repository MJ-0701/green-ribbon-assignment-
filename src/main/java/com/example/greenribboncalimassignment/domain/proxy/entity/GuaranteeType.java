package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.CodeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuaranteeType implements CodeValue {
    NORMAL_PREPAID("일반 선불", 0.15),
    NORMAL_POSTPAID("일반 후불", 0.20),
    SPECIAL_PREPAID("특급 선불", 0.25),
    SPECIAL_POSTPAID("특급 후불", 0.30);

    private final String description;
    private final double feeRate; // 수수료율 내장

    public long calculateFee(long missedAmount) {
        return (long) (missedAmount * feeRate);
    }

    public boolean isPrepaid() {
        return this == NORMAL_PREPAID || this == SPECIAL_PREPAID;
    }

    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
