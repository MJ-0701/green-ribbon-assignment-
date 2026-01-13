package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import com.example.greenribboncalimassignment.web.dto.response.QProxyRequestUnitResponse;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequest.proxyRequest;
import static com.example.greenribboncalimassignment.domain.proxy.entity.QProxyRequestUnit.proxyRequestUnit;
import static com.example.greenribboncalimassignment.domain.user.entity.QUserTreatment.userTreatment;

@RequiredArgsConstructor
public class UserTreatmentRepositoryImpl implements UserTreatmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 신청 가능한 진료 기록 조회
     * - 조건: 특정 유저의 진료 기록 중, 현재 진행 중(PENDING ~ COMPLETED/DISCLAIMER)인 대행 신청이 없는 건
     * - 필터링: 취소(CANCELLED)된 건은 재신청 가능하므로 조회 대상에 포함됨
     */
    @Override
    public Slice<ProxyRequestUnitResponse> findAvailableTreatments(Long userId, Pageable pageable) {

        List<ProxyRequestUnitResponse> content = queryFactory
                .select(new QProxyRequestUnitResponse(
                        userTreatment.hospital.id,         // treatmentId 자리에 대표값으로 hospitalId 사용
                        userTreatment.hospital.id,         // hospitalId
                        userTreatment.hospitalName,
                        userTreatment.treatmentDate.max(), // 해당 병원의 가장 최근 진료일
                        userTreatment.amount.sum()         // 병원별 금액 합산
                ))
                .from(userTreatment)
                .where(
                        // 1. 특정 유저의 진료 기록
                        userTreatment.user.id.eq(userId),

                        // 2. 이미 신청된 건(Active Request)에 포함되지 않은 개별 진료 기록만 필터링 후 합산
                        JPAExpressions
                                .selectOne()
                                .from(proxyRequestUnit)
                                .join(proxyRequestUnit.proxyRequest, proxyRequest)
                                .where(
                                        proxyRequestUnit.userTreatment.id.eq(userTreatment.id),
                                        proxyRequest.user.id.eq(userId),
                                        proxyRequest.status.ne(ProxyStatus.CANCELLED)
                                )
                                .notExists()
                )
                .groupBy(userTreatment.hospital.id, userTreatment.hospitalName) // 병원 단위 그룹화
                .orderBy(userTreatment.treatmentDate.max().desc())             // 최신 진료 발생 병원순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // Slice 처리 로직
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
