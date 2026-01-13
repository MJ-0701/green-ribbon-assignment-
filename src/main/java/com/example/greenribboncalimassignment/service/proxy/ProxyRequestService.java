package com.example.greenribboncalimassignment.service.proxy;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProxyRequestService {

    private final UsersRepository usersRepository;
    private final UserTreatmentRepository userTreatmentRepository;

    /**
     * 3.5 유저 진료 기록 조회 (신청 가능 목록)
     * - 이미 신청 중(PENDING ~ FEE_CLAIM)인 건은 제외하고 조회
     * - Slice Paging 적용 (무한 스크롤)
     */
    public Slice<ProxyRequestUnitResponse> getAvailableTreatments(Long userId, Pageable pageable) {
        // 1. 유저 검증
        if (!usersRepository.existsById(userId)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 조회 (필터링 로직은 서브쿼리로 처리 됩니다.)
        return userTreatmentRepository.findAvailableTreatments(userId, pageable);
    }
}
