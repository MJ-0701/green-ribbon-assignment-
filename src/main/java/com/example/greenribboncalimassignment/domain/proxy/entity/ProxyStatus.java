package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.CodeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ProxyStatus implements CodeValue {
    // 1. 상태 정의와 허용되는 다음 상태 목록 정의
    PENDING("대행 신청 완료") {
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return List.of(IN_PROGRESS, CANCELLED, DISCLAIMER); //
        }
    },
    IN_PROGRESS("서류 수집 및 보험사 청구 진행 중") {
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return List.of(FEE_CLAIM, DISCLAIMER); //
        }
    },
    FEE_CLAIM("수수료 안내") {
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return List.of(COMPLETED); //
        }
    },
    COMPLETED("수수료 결제 완료") { // 종결 상태
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return Collections.emptyList(); // 변경 불가
        }
    },
    CANCELLED("사용자 취소") { // 종결 상태
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return Collections.emptyList(); // 변경 불가
        }
    },
    DISCLAIMER("면책 종결") { // 종결 상태
        @Override
        public List<ProxyStatus> allowedNextStates() {
            return Collections.emptyList(); // 변경 불가
        }
    };

    private final String description;

    // 추상 메서드: 각 상태별로 오버라이딩
    public abstract List<ProxyStatus> allowedNextStates();

    // 검증 로직
    public boolean canTransitionTo(ProxyStatus nextStatus) {
        return allowedNextStates().contains(nextStatus);
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
