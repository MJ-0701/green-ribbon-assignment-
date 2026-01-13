package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.config.JpaConfig;
import com.example.greenribboncalimassignment.config.QueryDslConfig;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestUnit;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class}) // QueryDSL Config 주입
class UserTreatmentRepositoryTest {

    @Autowired
    private UserTreatmentRepository userTreatmentRepository;

    @Autowired
    private EntityManager em;

    private Users user;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        // 기본 데이터 세팅
        hospital = Hospital.of("테스트병원");
        em.persist(hospital);

        user = Users.of("채명정");
        em.persist(user);
    }

    @Test
    @DisplayName("신청 가능 목록 조회: 같은 병원의 진료 기록은 하나로 합쳐지고 금액이 합산된다.")
    void find_available_treatments_group_by_hospital() {
        // given
        // 1. [미신청] A병원 - 10,000원 (조회 대상)
        UserTreatment t1 = UserTreatment.of(user, hospital, LocalDate.now().minusDays(2), 10000L);
        em.persist(t1);

        // 2. [신청 완료] A병원 - 20,000원 (PENDING 상태 - 필터링되어 합산에서 제외되어야 함)
        UserTreatment t2_pending = UserTreatment.of(user, hospital, LocalDate.now().minusDays(1), 20000L);
        em.persist(t2_pending);
        createProxyRequest(user, t2_pending, ProxyStatus.PENDING);

        // 3. [신청 취소] A병원 - 30,000원 (CANCELLED 상태 - 재신청 가능하므로 합산에 포함되어야 함)
        UserTreatment t3_cancelled = UserTreatment.of(user, hospital, LocalDate.now(), 30000L);
        em.persist(t3_cancelled);
        createProxyRequest(user, t3_cancelled, ProxyStatus.CANCELLED);

        em.flush();
        em.clear();

        // when
        Slice<ProxyRequestUnitResponse> result = userTreatmentRepository.findAvailableTreatments(user.getId(), PageRequest.of(0, 10));

        // then
        List<ProxyRequestUnitResponse> content = result.getContent();

        // [검증 1] 병원별로 그룹화되었으므로 결과는 1줄이어야 함 (모두 같은 hospital이므로)
        assertThat(content).hasSize(1);

        ProxyRequestUnitResponse response = content.get(0);

        // [검증 2] 식별자 및 병원 정보 확인
        assertThat(response.hospitalId()).isEqualTo(hospital.getId());
        assertThat(response.hospitalName()).isEqualTo(hospital.getName());

        // [검증 3] 금액 합산 확인 (t1: 10,000 + t3: 30,000 = 40,000)
        // t2(PENDING)는 제외되어야 함
        assertThat(response.missedAmount()).isEqualTo(40000L);

        // [검증 4] 날짜 확인 (t1, t3 중 더 최신인 t3의 날짜여야 함)
        assertThat(response.treatmentDate()).isEqualTo(t3_cancelled.getTreatmentDate());
    }


    // --- Helper Method ---
    private void createProxyRequest(Users user, UserTreatment treatment, ProxyStatus status) {
        // 1. 보장 타입 선택 (예: 일반 후불)
        GuaranteeType guaranteeType = GuaranteeType.NORMAL_POSTPAID;

        // 2. 도메인 로직을 이용해 수수료 계산
        long calculatedFee = guaranteeType.calculateFee(treatment.getAmount());

        // 3. Entity 생성 (계산된 수수료 주입)
        ProxyRequest request = ProxyRequest.builder()
                .user(user)
                .status(status)
                .guaranteeType(guaranteeType)
                .feeAmount(calculatedFee)
                .totalMissedAmount(treatment.getAmount())
                .build();

        em.persist(request);

        ProxyRequestUnit unit = ProxyRequestUnit.builder()
                .proxyRequest(request)
                .userTreatment(treatment)
                .missedAmount(treatment.getAmount())
                .build();

        em.persist(unit);
    }

    @Test
    @DisplayName("선택한 병원 진료기록 조회: 유저 ID와 병원 ID 목록에 일치하는 진료기록만 반환한다.")
    void find_all_by_userid_and_hospital_ids() {
        // given
        Hospital h1 = Hospital.of("A병원");
        Hospital h2 = Hospital.of("B병원"); // 선택 대상
        Hospital h3 = Hospital.of("C병원"); // 선택 대상
        em.persist(h1);
        em.persist(h2);
        em.persist(h3);

        UserTreatment t1 = UserTreatment.of(user, h1, LocalDate.now(), 1000L); // 제외 대상
        UserTreatment t2 = UserTreatment.of(user, h2, LocalDate.now(), 2000L); // 포함 대상
        UserTreatment t3 = UserTreatment.of(user, h3, LocalDate.now(), 3000L); // 포함 대상
        em.persist(t1);
        em.persist(t2);
        em.persist(t3);

        em.flush();
        em.clear();

        // when
        List<Long> targetHospitalIds = List.of(h2.getId(), h3.getId());
        List<UserTreatment> result = userTreatmentRepository.findAllByUserIdAndHospital_IdIn(user.getId(), targetHospitalIds);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("hospitalName")
                .containsExactlyInAnyOrder("B병원", "C병원");
    }
}