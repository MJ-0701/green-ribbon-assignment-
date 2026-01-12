package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestResponse;

import java.util.List;

public interface ProxyRequestRepositoryCustom {

    // 1. 상세 조회
    ProxyRequestResponse findDtoById(Long proxyId);

    // 2. [검증] 중복 진행 체크
    // "동일 사용자가 아직 종결되지 않은 대행이 있다면 신청 불가"
    boolean existsOngoingRequest(Long userId);

    // 3. [검증] 재신청 유효성 체크
    // "종결(COMPLETED) 또는 면책(DISCLAIMER)된 병원은 다시 신청 불가능"
    // 반환값: 신청 불가한(이미 처리된) Treatment ID 목록
    List<Long> findAlreadyProcessedTreatmentIds(List<Long> treatmentIds);
}
