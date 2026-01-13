package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "proxy_request_histories")
@Comment("청구 대행 상태 변경 이력")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProxyRequestHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_id", nullable = false)
    @Comment("대상 신청서")
    private ProxyRequest proxyRequest;

    @Convert(converter = ProxyStatusConverter.class)
    @Column(nullable = false)
    @Comment("변경 전 상태")
    private ProxyStatus previousStatus;

    @Convert(converter = ProxyStatusConverter.class)
    @Column(nullable = false)
    @Comment("변경 후 상태")
    private ProxyStatus nextStatus;

    @Column
    @Comment("변경 사유 (면책 등)")
    private String reason;

    // 생성 메서드
    public static ProxyRequestHistory create(ProxyRequest proxyRequest, ProxyStatus prev, ProxyStatus next, String reason) {
        return ProxyRequestHistory.builder()
                .proxyRequest(proxyRequest)
                .previousStatus(prev)
                .nextStatus(next)
                .reason(reason)
                .build();
    }
}
