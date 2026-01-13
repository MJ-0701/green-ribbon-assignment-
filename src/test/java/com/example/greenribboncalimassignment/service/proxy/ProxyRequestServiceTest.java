package com.example.greenribboncalimassignment.service.proxy;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestHistory;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestHistoryRepository;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestRepository;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestUnitRepository;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.response.ProxyCreateResponse;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProxyRequestServiceTest {

    @InjectMocks
    private ProxyRequestService proxyRequestService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserTreatmentRepository userTreatmentRepository;

    @Mock
    private ProxyRequestRepository proxyRequestRepository;

    @Mock
    private ProxyRequestUnitRepository proxyRequestUnitRepository;

    @Mock
    private ProxyRequestHistoryRepository proxyRequestHistoryRepository;

    @Test
    @DisplayName("진료기록 조회 성공: 유저가 존재하면 Repository를 호출하여 결과를 반환한다.")
    void get_available_treatments_success() {
        // given
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(usersRepository.existsById(userId)).willReturn(true); // 유저 존재함

        Slice<ProxyRequestUnitResponse> expectedSlice = new SliceImpl<>(List.of());
        given(userTreatmentRepository.findAvailableTreatments(userId, pageable))
                .willReturn(expectedSlice);

        // when
        Slice<ProxyRequestUnitResponse> result = proxyRequestService.getAvailableTreatments(userId, pageable);

        // then
        assertThat(result).isEqualTo(expectedSlice);
        then(userTreatmentRepository).should().findAvailableTreatments(userId, pageable); // 호출 확인
    }

    @Test
    @DisplayName("진료기록 조회 실패: 유저가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다.")
    void get_available_treatments_fail_user_not_found() {
        // given
        Long userId = 999L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(usersRepository.existsById(userId)).willReturn(false); // 유저 없음

        // when & then
        assertThatThrownBy(() -> proxyRequestService.getAvailableTreatments(userId, pageable))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.USER_NOT_FOUND);

        // Repository는 호출되지 않아야 함
        then(userTreatmentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("신청 성공: 병원별 합산 금액과 수수료가 포함된 ProxyCreateResponse를 반환한다.")
    void create_proxy_request_success() {
        // given
        Long userId = 1L;
        List<Long> hospitalIds = List.of(101L, 102L);
        ProxyCreateRequest request = new ProxyCreateRequest(userId, GuaranteeType.NORMAL_POSTPAID, hospitalIds);

        Users user = Users.of("채명정");

        // Hospital 엔티티 구조에 맞게 생성 (of 메서드 사용)
        Hospital h1 = Hospital.of("서울병원");
        Hospital h2 = Hospital.of("경기병원");

        // Mock 조회 로직을 위해 Reflection으로 ID 강제 주입
        ReflectionTestUtils.setField(h1, "id", 101L);
        ReflectionTestUtils.setField(h2, "id", 102L);

        // 병원 101: 합계 30,000 / 병원 102: 합계 50,000 -> 총 80,000
        UserTreatment t1 = UserTreatment.of(user, h1, LocalDate.now(), 10000L);
        UserTreatment t2 = UserTreatment.of(user, h1, LocalDate.now(), 20000L);
        UserTreatment t3 = UserTreatment.of(user, h2, LocalDate.now(), 50000L);
        List<UserTreatment> treatments = List.of(t1, t2, t3);

        given(usersRepository.findById(userId)).willReturn(Optional.of(user));
        given(proxyRequestRepository.existsOngoingRequest(userId)).willReturn(false);
        given(userTreatmentRepository.findAllByUserIdAndHospital_IdIn(userId, hospitalIds)).willReturn(treatments);
        given(proxyRequestUnitRepository.existsByUserTreatmentIdInAndProxyRequest_StatusIn(anyList(), anyList()))
                .willReturn(false);

        // save 시 ProxyRequest에도 ID가 생성된 것처럼 시뮬레이션
        given(proxyRequestRepository.save(any(ProxyRequest.class))).willAnswer(invocation -> {
            ProxyRequest pr = invocation.getArgument(0);
            ReflectionTestUtils.setField(pr, "id", 1L); // 명세의 proxyRequestId 반환을 위해 필요
            return pr;
        });

        // when
        ProxyCreateResponse response = proxyRequestService.createProxyRequest(request);

        // then
        assertThat(response.proxyRequestId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ProxyStatus.PENDING);
        assertThat(response.totalMissedAmount()).isEqualTo(80000L);

        // 일반 후불(20%) 수수료: 80,000 * 0.2 = 16,000
        assertThat(response.feeAmount()).isEqualTo(16000L);

        // Side Effect 검증
        then(proxyRequestHistoryRepository).should(times(1)).save(any(ProxyRequestHistory.class));
    }

    @Test
    @DisplayName("신청 실패: 요청한 진료 기록 중 이미 처리 완료(COMPLETED)된 건이 포함되어 있으면 예외가 발생한다.")
    void create_proxy_request_fail_already_processed() {
        // given
        Long userId = 1L;
        List<Long> hospitalIds = List.of(101L);
        ProxyCreateRequest request = new ProxyCreateRequest(userId, GuaranteeType.NORMAL_POSTPAID, hospitalIds);

        Users user = Users.of("채명정");
        Hospital h1 = Hospital.of("테스트병원");
        ReflectionTestUtils.setField(h1, "id", 101L); // ID 주입

        UserTreatment t1 = UserTreatment.of(user, h1, LocalDate.now(), 10000L);
        // t1에 ID가 있어야 exists... 쿼리 파라미터로 들어갈 때 의미가 있음
        ReflectionTestUtils.setField(t1, "id", 500L);

        given(usersRepository.findById(userId)).willReturn(Optional.of(user));
        given(proxyRequestRepository.existsOngoingRequest(userId)).willReturn(false);
        given(userTreatmentRepository.findAllByUserIdAndHospital_IdIn(userId, hospitalIds))
                .willReturn(List.of(t1));

        // 핵심: Repository가 이미 처리된(COMPLETED, DISCLAIMER) 신청에 묶인 Unit이 있다고 응답하도록 설정
        given(proxyRequestUnitRepository.existsByUserTreatmentIdInAndProxyRequest_StatusIn(anyList(), anyList()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> proxyRequestService.createProxyRequest(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.ALREADY_PROCESSED_TREATMENT);

        // 1. 저장은 절대로 호출되지 않아야 함
        then(proxyRequestRepository).should(never()).save(any(ProxyRequest.class));

        // 2. 이력 저장소도 아무런 상호작용이 없어야 함
        then(proxyRequestHistoryRepository).shouldHaveNoInteractions();
    }
}