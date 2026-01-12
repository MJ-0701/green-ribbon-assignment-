package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestResponse;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import com.example.greenribboncalimassignment.web.dto.response.QProxyRequestUnitResponse;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequest.proxyRequest;
import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequestUnit.proxyRequestUnit;
import static com.example.greenribboncalimassignment.domain.user.entity.QUserTreatment.userTreatment;
import static com.example.greenribboncalimassignment.domain.user.entity.QUsers.users;

@RequiredArgsConstructor
public class ProxyRequestRepositoryImpl implements ProxyRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ProxyRequestResponse findDtoById(Long proxyId) {

        Tuple parent = queryFactory
                .select(
                        proxyRequest.id,
                        users.name,
                        proxyRequest.guaranteeType,
                        proxyRequest.status,
                        proxyRequest.totalMissedAmount,
                        proxyRequest.feeAmount,
                        proxyRequest.createdAt
                )
                .from(proxyRequest)
                .join(proxyRequest.user, users)
                .where(proxyRequest.id.eq(proxyId))
                .fetchOne();

        if (parent == null) {
            return null;
        }

        List<ProxyRequestUnitResponse> units = queryFactory
                .select(new QProxyRequestUnitResponse(
                        proxyRequestUnit.id,
                        userTreatment.hospitalName, // Snapshot 사용
                        userTreatment.treatmentDate,
                        proxyRequestUnit.missedAmount
                ))
                .from(proxyRequestUnit)
                .join(proxyRequestUnit.userTreatment, userTreatment)
                .where(proxyRequestUnit.proxyRequest.id.eq(proxyId))
                .fetch();

        return new ProxyRequestResponse(
                parent.get(proxyRequest.id),
                parent.get(users.name),
                parent.get(proxyRequest.guaranteeType),
                parent.get(proxyRequest.status),
                parent.get(proxyRequest.totalMissedAmount),
                parent.get(proxyRequest.feeAmount),
                parent.get(proxyRequest.createdAt),
                units // 자식 리스트 주입
        );
    }

    /**
     * 검증 1: 진행 중인 신청 건 존재 여부 (Limit 1 최적화)
     */
    @Override
    public boolean existsOngoingRequest(Long userId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(proxyRequest)
                .where(
                        proxyRequest.user.id.eq(userId),
                        // 종결 상태가 아니면 "진행 중"으로 간주
                        proxyRequest.status.notIn(
                                ProxyStatus.COMPLETED,
                                ProxyStatus.CANCELLED,
                                ProxyStatus.DISCLAIMER
                        )
                )
                .fetchFirst(); // limit 1

        return fetchOne != null;
    }

    /**
     * 검증 2: 이미 처리된(재신청 불가) 진료기록 ID 조회
     * "사용자 취소(CANCELLED)"된 건은 조회되지 않음 -> 재신청 가능
     */
    @Override
    public List<Long> findAlreadyProcessedTreatmentIds(List<Long> treatmentIds) {
        return queryFactory
                .select(proxyRequestUnit.userTreatment.id)
                .from(proxyRequestUnit)
                .join(proxyRequestUnit.proxyRequest, proxyRequest)
                .where(
                        proxyRequestUnit.userTreatment.id.in(treatmentIds),
                        // 재신청 불가능한 상태: 정상 완료(COMPLETED) or 면책(DISCLAIMER)
                        proxyRequest.status.in(
                                ProxyStatus.COMPLETED,
                                ProxyStatus.DISCLAIMER
                        )
                )
                .fetch();
    }
}
