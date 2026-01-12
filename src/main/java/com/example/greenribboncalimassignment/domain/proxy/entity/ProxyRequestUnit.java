package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "proxy_request_units")
@Comment("병원별 청구 대행 단위")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProxyRequestUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    @Comment("대행 단위 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_id", nullable = false)
    @Comment("소속된 청구 대행 신청서")
    private ProxyRequest proxyRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    @Comment("원본 진료 기록 (병원 정보 포함)")
    private UserTreatment userTreatment;

    @Column(nullable = false)
    @Comment("놓친 보험금 (신청 당시 금액)")
    private Long missedAmount;

    public Long getHospitalId() {
        return this.userTreatment.getHospital().getId();
    }

    protected void assignProxyRequest(ProxyRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
    }
}