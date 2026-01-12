package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "proxy_requests")
@Comment("청구 대행 신청 (Aggregate Root)")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProxyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proxy_id")
    @Comment("청구 대행 신청 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("신청한 유저")
    private Users user;

    @Convert(converter = ProxyStatusConverter.class)
    @Column(nullable = false)
    @Comment("진행 상태 (PENDING, IN_PROGRESS 등)")
    private ProxyStatus status;

    @Convert(converter = GuaranteeTypeConverter.class)
    @Column(nullable = false)
    @Comment("보장 타입 (수수료율 결정)")
    private GuaranteeType guaranteeType;

    @Column(nullable = false)
    @Comment("총 놓친 보험금 (신청 단위 합계)")
    private Long totalMissedAmount;

    @Column(nullable = false)
    @Comment("예상 수수료 (보장 타입에 따라 계산)")
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
