package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserTreatmentRepositoryCustom {

    /**
     * 해당 유저의 진료 기록 중, 신청 가능한(이미 진행 중인 건 제외) 목록을 조회합니다. (Slice 페이징)
     */
    Slice<ProxyRequestUnitResponse> findAvailableTreatments(Long userId, Pageable pageable);
}
