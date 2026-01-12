package com.example.greenribboncalimassignment.domain.proxy.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyStatusTest {

    @DisplayName("정의된 상태 흐름대로 전이가 가능해야 한다")
    @Test
    void canTransitionTo_success() {
        // PENDING -> IN_PROGRESS (O)
        assertThat(ProxyStatus.PENDING.canTransitionTo(ProxyStatus.IN_PROGRESS)).isTrue();

        // IN_PROGRESS -> FEE_CLAIM (O)
        assertThat(ProxyStatus.IN_PROGRESS.canTransitionTo(ProxyStatus.FEE_CLAIM)).isTrue();
    }

    @DisplayName("정의되지 않은 상태 흐름은 차단되어야 한다")
    @Test
    void canTransitionTo_fail() {
        // PENDING -> COMPLETED (X, 건너뛰기 불가)
        assertThat(ProxyStatus.PENDING.canTransitionTo(ProxyStatus.COMPLETED)).isFalse();

        // COMPLETED -> IN_PROGRESS (X, 역행 불가)
        assertThat(ProxyStatus.COMPLETED.canTransitionTo(ProxyStatus.IN_PROGRESS)).isFalse();
    }
}