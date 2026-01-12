package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "proxy_request_units")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProxyRequestUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_id", nullable = false)
    private ProxyRequest proxyRequest;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private UserTreatment userTreatment;

    @Column(nullable = false)
    private Long missedAmount;

    public Long getHospitalId() {
        return this.userTreatment.getHospital().getId();
    }

    protected void assignProxyRequest(ProxyRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
    }
}