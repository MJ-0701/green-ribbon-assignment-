package com.example.greenribboncalimassignment.integration;

import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.hospital.repository.HospitalRepository;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.proxy.repository.ProxyRequestRepository;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.request.ProxyStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 종료 후 DB 롤백 (데이터 격리)
class ProxyRequestIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UsersRepository usersRepository;
    @Autowired HospitalRepository hospitalRepository;
    @Autowired UserTreatmentRepository userTreatmentRepository;
    @Autowired ProxyRequestRepository proxyRequestRepository;

    private Users user;
    private Hospital hospital;
    private UserTreatment treatment1;
    private UserTreatment treatment2;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        user = Users.of("채명정");
        usersRepository.save(user);

        // 2. 병원 생성
        hospital = Hospital.builder()
                .name("서울대병원")
                .build();
        hospitalRepository.save(hospital);

        // 3. 진료 기록 생성
        treatment1 = UserTreatment.of(user, hospital, LocalDate.now().minusDays(5), 10000L);
        treatment2 = UserTreatment.of(user, hospital, LocalDate.now().minusDays(3), 20000L);
        userTreatmentRepository.saveAll(List.of(treatment1, treatment2));
    }

    @Test
    @DisplayName("3.1 신청 성공: 정상적인 요청 시 PENDING 상태로 저장된다.")
    void create_proxy_request_success() throws Exception {
        // given
        ProxyCreateRequest request = new ProxyCreateRequest(
                user.getId(),
                GuaranteeType.NORMAL_POSTPAID,
                List.of(hospital.getId())
        );

        // when & then
        mockMvc.perform(post("/api/proxy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // DB 검증
        List<ProxyRequest> all = proxyRequestRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getStatus()).isEqualTo(ProxyStatus.PENDING);
        assertThat(all.get(0).getTotalMissedAmount()).isEqualTo(30000L); // 10000 + 20000
    }


    @Test
    @DisplayName("3.1 신청 실패: 이미 진행 중인 건이 있으면 중복 신청 에러(4201)가 발생한다.")
    void create_proxy_request_fail_duplicate() throws Exception {
        // given
        // 미리 진행 중인 건 생성
        ProxyRequest existingRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        proxyRequestRepository.save(existingRequest);

        ProxyCreateRequest newRequest = new ProxyCreateRequest(
                user.getId(),
                GuaranteeType.NORMAL_POSTPAID,
                List.of(hospital.getId())
        );

        // when & then
        mockMvc.perform(post("/api/proxy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResultCode.DUPLICATE_REQUEST_NOT_ALLOWED.getCode()));
    }

    @Test
    @DisplayName("3.3 상태 변경: 선불(Prepaid) 건은 '수수료 청구' 요청 시 자동으로 '결제 완료'가 된다.")
    void update_status_prepaid_auto_complete() throws Exception {
        // given
        // 선불 + 진행중 상태의 신청서 준비
        ProxyRequest prepaidRequest = ProxyRequest.builder()
                .user(user)
                .guaranteeType(GuaranteeType.NORMAL_PREPAID) // 선불
                .status(ProxyStatus.IN_PROGRESS)      // 진행중
                .totalMissedAmount(50000L).feeAmount(5000L)
                .build();
        proxyRequestRepository.save(prepaidRequest);

        // 변경 요청: 수수료 안내(FEE_CLAIM)
        ProxyStatusUpdateRequest updateRequest = new ProxyStatusUpdateRequest(ProxyStatus.FEE_CLAIM, null);

        // when
        mockMvc.perform(patch("/api/proxy-requests/{id}/status", prepaidRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // then: DB 확인 (COMPLETED여야 함)
        ProxyRequest updated = proxyRequestRepository.findById(prepaidRequest.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ProxyStatus.COMPLETED);
    }

    @Test
    @DisplayName("3.4 취소 성공: PENDING 상태인 건은 취소(CANCELLED) 처리가 된다.")
    void delete_proxy_request_success() throws Exception {
        // given
        ProxyRequest pendingRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        // 기본 상태 PENDING
        proxyRequestRepository.save(pendingRequest);

        // when
        mockMvc.perform(delete("/api/proxy-requests/{id}", pendingRequest.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // then
        ProxyRequest cancelled = proxyRequestRepository.findById(pendingRequest.getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(ProxyStatus.CANCELLED);
    }

    @Test
    @DisplayName("3.4 취소 실패: 이미 진행 중(IN_PROGRESS)인 건은 취소할 수 없다.")
    void delete_proxy_request_fail_invalid_status() throws Exception {
        // given
        ProxyRequest processingRequest = ProxyRequest.builder()
                .user(user)
                .guaranteeType(GuaranteeType.NORMAL_POSTPAID)
                .status(ProxyStatus.IN_PROGRESS) // 진행중
                .totalMissedAmount(10000L).feeAmount(1000L)
                .build();
        proxyRequestRepository.save(processingRequest);

        // when & then
        mockMvc.perform(delete("/api/proxy-requests/{id}", processingRequest.getId()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ResultCode.CANCEL_ONLY_AT_PENDING.getCode()));
    }
}
