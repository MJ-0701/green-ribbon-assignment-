package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "proxy_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProxyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proxy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Convert(converter = ProxyStatusConverter.class)
    @Column(nullable = false)
    private ProxyStatus status;

    @Convert(converter = GuaranteeTypeConverter.class)
    @Column(nullable = false)
    private GuaranteeType guaranteeType;

    @Column(nullable = false)
    private Long totalMissedAmount;

    @Column(nullable = false)
    private Long feeAmount;

    @OneToMany(mappedBy = "proxyRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProxyRequestUnit> proxyRequestUnits = new ArrayList<>();

    // 대행 단위 추가 및 금액 재계산
    public void addUnit(ProxyRequestUnit unit) {
        this.proxyRequestUnits.add(unit);
        unit.assignProxyRequest(this); // 연관관계 편의 메서드
        recalculateAmounts();
    }

    private void recalculateAmounts() {
        this.totalMissedAmount = proxyRequestUnits.stream()
                .mapToLong(ProxyRequestUnit::getMissedAmount)
                .sum();
        this.feeAmount = this.guaranteeType.calculateFee(this.totalMissedAmount);
    }

    public void updateStatus(ProxyStatus requestStatus) {
        // 1. 상태 전이 유효성 검증 (Smart Enum 활용)
        if (!this.status.canTransitionTo(requestStatus)) {
            throw new IllegalArgumentException(
                    String.format("상태 변경 불가: %s -> %s", this.status, requestStatus)
            );
        }

        // 2. 선불 타입 특수 로직 처리
        // "선불 타입의 경우 IN_PROGRESS → FEE_CLAIM 요청 시 자동으로 COMPLETED 로 전환"
        if (this.guaranteeType.isPrepaid()
                && this.status == ProxyStatus.IN_PROGRESS
                && requestStatus == ProxyStatus.FEE_CLAIM) {
            this.status = ProxyStatus.COMPLETED;
        } else {
            // 3. 일반적인 상태 변경
            this.status = requestStatus;
        }
    }
}
