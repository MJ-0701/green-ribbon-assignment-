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

    @Override
    public Slice<ProxyRequestUnitResponse> findAvailableTreatments(Long userId, Pageable pageable) {

        List<ProxyRequestUnitResponse> content = queryFactory
                .select(new QProxyRequestUnitResponse(
                        userTreatment.id,
                        userTreatment.hospitalName,
                        userTreatment.treatmentDate,
                        userTreatment.amount
                ))
                .from(userTreatment)
                .where(
                        // 1. 특정 유저의 진료 기록
                        userTreatment.user.id.eq(userId),

                        // 2. 이미 신청된 건(Active Request)이 "존재하지 않는(NOT EXISTS)" 것만 조회
                        JPAExpressions
                                .selectOne() // 1만 조회 (데이터 확인용)
                                .from(proxyRequestUnit)
                                .join(proxyRequestUnit.proxyRequest, proxyRequest)
                                .where(
                                        // 메인 쿼리의 treatment_id와 매칭 (상관 서브쿼리)
                                        proxyRequestUnit.userTreatment.id.eq(userTreatment.id),
                                        // 신청자의 ID 검증 (인덱스 힌트 역할)
                                        proxyRequest.user.id.eq(userId),
                                        // 취소된 건은 제외하고, 살아있는 신청 건만 체크
                                        proxyRequest.status.ne(ProxyStatus.CANCELLED)
                                )
                                .notExists() // 존재하지 않아야 함
                )
                .orderBy(userTreatment.treatmentDate.desc()) // 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // Slice 체크용 (+1)
                .fetch();

        // Slice 반환 로직 (hasNext 계산)
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
