package com.example.greenribboncalimassignment.domain.proxy.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class GuaranteeTypeTest {

    @DisplayName("보장 타입별 수수료율 계산이 정확해야 한다 (문서 2.2항 검증)")
    @ParameterizedTest
    @CsvSource({
            "NORMAL_PREPAID, 1000000, 150000",  // 일반 선불 15%
            "NORMAL_POSTPAID, 1000000, 200000", // 일반 후불 20%
            "SPECIAL_PREPAID, 1000000, 250000", // 특급 선불 25%
            "SPECIAL_POSTPAID, 1000000, 300000" // 특급 후불 30%
    })
    void calculateFee_test(GuaranteeType type, long missedAmount, long expectedFee) {
        // when
        long result = type.calculateFee(missedAmount);

        // then
        assertThat(result).isEqualTo(expectedFee);
    }

    @DisplayName("선불 타입 여부를 정확히 판단해야 한다")
    @Test
    void isPrepaid_test() {
        // then
        assertThat(GuaranteeType.NORMAL_PREPAID.isPrepaid()).isTrue();
        assertThat(GuaranteeType.SPECIAL_PREPAID.isPrepaid()).isTrue();

        assertThat(GuaranteeType.NORMAL_POSTPAID.isPrepaid()).isFalse();
        assertThat(GuaranteeType.SPECIAL_POSTPAID.isPrepaid()).isFalse();
    }
}