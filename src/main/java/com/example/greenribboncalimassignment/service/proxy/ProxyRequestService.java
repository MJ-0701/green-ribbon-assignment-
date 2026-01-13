package com.example.greenribboncalimassignment.service.proxy;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestHistory;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestUnit;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestHistoryRepository;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestRepository;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestUnitRepository;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.request.ProxyStatusUpdateRequest;
import com.example.greenribboncalimassignment.web.dto.response.ProxyCreateResponse;
import com.example.greenribboncalimassignment.web.dto.response.ProxyDetailResponse;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProxyRequestService {

    private final UsersRepository usersRepository;
    private final UserTreatmentRepository userTreatmentRepository;
    private final ProxyRequestRepository proxyRequestRepository;
    private final ProxyRequestUnitRepository proxyRequestUnitRepository;
    private final ProxyRequestHistoryRepository proxyRequestHistoryRepository;

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

    /**
     * 3.1 청구 대행 신청
     * [Process]
     * 1. 유저 유효성 및 신청 정책 검증 (1인 1진행중 원칙)
     * 2. 병원 ID 목록 기반 진료 기록 조회 및 종결 건 포함 여부 검증 -> 수정 : 병원 ID 목록 기반 재신청 가능 여부 검증 (종결된 병원 제외)
     * 3. 진료 기록 조회
     * 4. 병원별 금액 합산 및 ProxyRequest/Unit 생성 (병원당 1 Unit)
     * 5. 신청서 저장 및 초기 상태(PENDING) 이력 저장
     */
    @Transactional
    public ProxyCreateResponse createProxyRequest(ProxyCreateRequest request) {
        // 1. 유저 조회
        Users user = usersRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 2. 정책 검증: 동일 유저의 진행 중인 신청 건 존재 시 차단
        if (proxyRequestRepository.existsOngoingRequest(request.userId())) {
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST_NOT_ALLOWED);
        }

        // 3. 진료 기록 조회 전에 병원 ID만으로 먼저 체크
        validateHospitalAvailability(request.userId(), request.hospitalIds());

        // 4. 신청 대상 진료 기록 조회 (병원 ID 목록 기반)
        List<UserTreatment> treatments = userTreatmentRepository.findAllByUserIdAndHospital_IdIn(request.userId(), request.hospitalIds());
        if (treatments.isEmpty()) {
            throw new BusinessException(ResultCode.TREATMENT_NOT_FOUND);
        }

        // 5. ProxyRequest (Aggregate Root) 생성
        ProxyRequest proxyRequest = ProxyRequest.of(user, request.guaranteeType());

        // 6. 병원별 그룹화 및 병원당 1개의 Unit으로 합산 생성
        Map<Long, List<UserTreatment>> groupedByHospital = treatments.stream()
                .collect(Collectors.groupingBy(t -> t.getHospital().getId()));

        groupedByHospital.forEach((hospitalId, hospitalTreatments) -> {
            long totalHospitalAmount = hospitalTreatments.stream()
                    .mapToLong(UserTreatment::getAmount)
                    .sum();

            // 도메인 활용: 병원별 합산 금액을 가진 Unit 생성
            ProxyRequestUnit unit = ProxyRequestUnit.builder()
                    .userTreatment(hospitalTreatments.get(0)) // 대표 진료기록 (병원 정보 참조용)
                    .missedAmount(totalHospitalAmount)
                    .build();

            proxyRequest.addUnit(unit); // 내부적으로 합계/수수료 재계산 수행
        });

        // 7. DB 저장
        ProxyRequest savedRequest = proxyRequestRepository.save(proxyRequest);

        // 8. 최초 신청 이력 저장
        saveHistory(
                savedRequest,
                null,
                ProxyStatus.PENDING,
                String.format("신규 대행 신청 접수 (병원 %d곳 합산)", groupedByHospital.size())
        );

        return ProxyCreateResponse.from(savedRequest);
    }

    /**
     * 3.2 청구 대행 상세 조회
     */
    public ProxyDetailResponse getProxyRequestDetail(Long proxyRequestId) {
        ProxyDetailResponse response = proxyRequestRepository.findProxyDetail(proxyRequestId);

        if (response == null) {
            throw new BusinessException(ResultCode.PROXY_REQUEST_NOT_FOUND);
        }

        return response;
    }

    public List<ProxyDetailResponse.ProxyInfoDto> getProxyRequestList(Long userId) {
        // 유저 존재 확인 (선택 사항, 필요 시 주석 해제)
        // if (!usersRepository.existsById(userId)) throw new BusinessException(ResultCode.USER_NOT_FOUND);

        return proxyRequestRepository.findAllByUserId(userId);
    }

    /**
     * 3.3 청구 대행 상태 변경
     */
    @Transactional
    public void updateProxyRequestStatus(Long proxyRequestId, ProxyStatusUpdateRequest request) {
        // 1. 조회
        ProxyRequest proxyRequest = proxyRequestRepository.findById(proxyRequestId)
                .orElseThrow(() -> new BusinessException(ResultCode.PROXY_REQUEST_NOT_FOUND));

        ProxyStatus previousStatus = proxyRequest.getStatus();
        ProxyStatus requestedStatus = request.status();

        // 2. 상태 변경 (도메인 엔티티 로직 호출)
        try {
            proxyRequest.updateStatus(requestedStatus);
        } catch (BusinessException e) {
            // 엔티티에서 던진 예외를 그대로 전파
            throw e;
        }

        // 3. 변경된 최종 상태 확인
        ProxyStatus actualFinalStatus = proxyRequest.getStatus();

        // 4. 이력 저장
        String historyReason = request.reason();

        // 선불 자동 완료 케이스에 대한 사유 자동 기입 (선택)
        if (requestedStatus == ProxyStatus.FEE_CLAIM && actualFinalStatus == ProxyStatus.COMPLETED) {
            historyReason = "선불 건 수수료 안내 요청에 의한 자동 결제 완료 처리";
        }

        saveHistory(
                proxyRequest,
                previousStatus,
                actualFinalStatus,
                historyReason != null ? historyReason : "상태 변경 API 호출"
        );
    }

    /**
     * 3.4 청구 대행 취소
     * - Soft Delete 수행
     */
    @Transactional
    public void deleteProxyRequest(Long proxyRequestId) {
        ProxyRequest proxyRequest = proxyRequestRepository.findById(proxyRequestId)
                .orElseThrow(() -> new BusinessException(ResultCode.PROXY_REQUEST_NOT_FOUND));

        // 도메인 엔티티의 삭제 로직 호출 (상태 검증 포함)
        proxyRequest.delete();
    }


    // 수정 -> 종결(COMPLETED) 또는 면책(DISCLAIMER)된 병원은 다시 신청 불가능
    private void validateHospitalAvailability(Long userId, List<Long> hospitalIds) {
        boolean isBlocked = proxyRequestUnitRepository.existsProcessedHospital(userId, hospitalIds);

        if (isBlocked) {
            throw new BusinessException(ResultCode.ALREADY_PROCESSED_TREATMENT);
        }
    }

    private void saveHistory(ProxyRequest request, ProxyStatus prev, ProxyStatus next, String reason) {
        ProxyRequestHistory history = ProxyRequestHistory.create(request, prev, next, reason);
        proxyRequestHistoryRepository.save(history);
    }
}
