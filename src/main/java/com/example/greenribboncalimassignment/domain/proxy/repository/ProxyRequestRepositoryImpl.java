package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.web.dto.response.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequest.proxyRequest;
import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequestHistory.proxyRequestHistory;
import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequestUnit.proxyRequestUnit;
import static com.example.greenribboncalimassignment.domain.user.entity.QUserTreatment.userTreatment;
import static com.example.greenribboncalimassignment.domain.user.entity.QUsers.users;

@RequiredArgsConstructor
public class ProxyRequestRepositoryImpl implements ProxyRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ProxyDetailResponse findProxyDetail(Long proxyRequestId) {

        // 1. 신청서 기본 정보 조회
        ProxyDetailResponse.ProxyInfoDto proxyInfo = queryFactory
                .select(new QProxyDetailResponse_ProxyInfoDto(
                        proxyRequest.id,
                        proxyRequest.user.id,
                        users.name,
                        proxyRequest.guaranteeType,
                        proxyRequest.status,
                        proxyRequest.totalMissedAmount,
                        proxyRequest.feeAmount,
                        proxyRequest.createdAt
                ))
                .from(proxyRequest)
                .join(proxyRequest.user, users) // 일반 Join (Fetch X)
                .where(proxyRequest.id.eq(proxyRequestId))
                .fetchOne();

        // 존재하지 않으면 null 반환 (Service에서 예외 처리)
        if (proxyInfo == null) {
            return null;
        }

        // 2. 병원별 신청 내역 조회 (Units)
        List<ProxyDetailResponse.ProxyUnitDto> units = queryFactory
                .select(new QProxyDetailResponse_ProxyUnitDto(
                        proxyRequestUnit.id,
                        userTreatment.hospitalName,
                        proxyRequestUnit.missedAmount,
                        userTreatment.treatmentDate
                ))
                .from(proxyRequestUnit)
                .join(proxyRequestUnit.userTreatment, userTreatment)
                .where(proxyRequestUnit.proxyRequest.id.eq(proxyRequestId))
                .fetch();

        // 3. 상태 변경 이력 조회 (Histories)
        List<ProxyDetailResponse.ProxyHistoryDto> histories = queryFactory
                .select(new QProxyDetailResponse_ProxyHistoryDto(
                        proxyRequestHistory.nextStatus,
                        proxyRequestHistory.reason,
                        proxyRequestHistory.createdAt
                ))
                .from(proxyRequestHistory)
                .where(proxyRequestHistory.proxyRequest.id.eq(proxyRequestId))
                .orderBy(proxyRequestHistory.createdAt.desc())
                .fetch();

        // 4. 결과 조합
        return new ProxyDetailResponse(proxyInfo, units, histories);
    }

    @Override
    public List<ProxyDetailResponse.ProxyInfoDto> findAllByUserId(Long userId) {
        return queryFactory
                .select(new QProxyDetailResponse_ProxyInfoDto(
                        proxyRequest.id,
                        proxyRequest.user.id,
                        users.name,
                        proxyRequest.guaranteeType,
                        proxyRequest.status,
                        proxyRequest.totalMissedAmount,
                        proxyRequest.feeAmount,
                        proxyRequest.createdAt
                ))
                .from(proxyRequest)
                .join(proxyRequest.user, users)
                .where(proxyRequest.user.id.eq(userId)) // 유저 ID 조건
                .orderBy(proxyRequest.createdAt.desc()) // 최신순 정렬
                .fetch();
    }
}
