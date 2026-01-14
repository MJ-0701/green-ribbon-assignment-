package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProxyRequestTest {

    private Users user;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        // 테스트용 더미 데이터 (ID는 null이어도 됨)
        user = Users.builder().name("채명정").build();
        hospital = Hospital.builder().name("세브란스").build();
    }

    @DisplayName("Unit이 추가되면 총 금액과 수수료가 자동으로 재계산되어야 한다")
    @Test
    void addUnit_recalculate_test() {
        // given (일반 선불 15%)
        ProxyRequest proxyRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_PREPAID);

        UserTreatment treatment1 = UserTreatment.of(user, hospital, LocalDate.now(), 100_000L); // 10만원
        UserTreatment treatment2 = UserTreatment.of(user, hospital, LocalDate.now(), 200_000L); // 20만원

        // when
        proxyRequest.addUnit(ProxyRequestUnit.from(treatment1));
        proxyRequest.addUnit(ProxyRequestUnit.from(treatment2));

        // then
        // 총 금액: 300,000
        assertThat(proxyRequest.getTotalMissedAmount()).isEqualTo(300_000L);
        // 수수료: 300,000 * 0.15 = 45,000
        assertThat(proxyRequest.getFeeAmount()).isEqualTo(45_000L);
    }

    @DisplayName("선불 타입은 '수수료 안내' 요청 시 자동으로 '결제 완료' 상태가 되어야 한다 (문서 2.3항)")
    @Test
    void prepaid_auto_complete_test() {
        // given (선불 타입 + 현재 처리중 상태)
        ProxyRequest proxyRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_PREPAID);
        proxyRequest.updateStatus(ProxyStatus.IN_PROGRESS); // 강제 상태 변경 (테스트용)

        // when (수수료 안내 상태로 변경 시도)
        proxyRequest.updateStatus(ProxyStatus.FEE_CLAIM);

        // then (결제 완료로 자동 점프)
        assertThat(proxyRequest.getStatus()).isEqualTo(ProxyStatus.COMPLETED);
    }

    @DisplayName("잘못된 상태 변경 시 BusinessException이 발생해야 한다")
    @Test
    void invalid_transition_exception_test() {
        // given (PENDING 상태)
        ProxyRequest proxyRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_PREPAID);

        // when & then (PENDING -> COMPLETED 바로 가기 시도)
        assertThatThrownBy(() -> proxyRequest.updateStatus(ProxyStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.INVALID_STATUS_TRANSITION);
    }
}